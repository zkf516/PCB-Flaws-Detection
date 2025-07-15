import os
import util
from flask import request
import logging
import sys
import io
from PIL import Image
import time
import json
import util.draw_boxes
import util.inference_local_v9
from backend_model.postgres import PostgresModel
#import util.inference_token
#import util.inference_ak_sk
#from src.yolo_v9.utils.plots import Annotator, colors, save_one_box

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/..")

class InferenceController:
    def __init__(self) -> None:
        self.local_model_busy = False
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(logging.INFO)

    def do_inference(self, path, filename):
        result = None
        source = None

        if self.local_model_busy:
            self.logger.info("local model is busy, return error")
            result = {
                "success": False,
                "result": result,
                "filename": f"{filename}",
                "source": source
            }
            return result
        
        # 使用本地模型推理
        self.local_model_busy = True
        self.logger.info("start edge inference")
        try :
            print("loading model" + os.environ["MODEL_PATH"])
            local_model = util.inference_local_v9.InferenceLocalV9(os.environ["MODEL_PATH"])
            result = local_model.infer(path)
        except Exception as e:
            self.logger.error(e)
            result = {
                "error_code": 500,
                "error_msg": f"Internal Server Error, {e}"
            }
            self.local_model_busy = False
        self.logger.info("finished edge inference")
        self.local_model_busy = False
        source = "Edge Server"
        
        # 构建响应
        result = {
            "success": True,
            "result": result,
            "filename": f"{filename}",
            "source": source
        }

        # 存储结果到数据库
        PostgresModel().insert_inference_result(filename,result)

        # 将检测结果标注在图上
        if result["success"]:
            # print("zkfDEBUG result:", result) 
            util.draw_boxes.draw_boxes(path, result["result"])
        return result
    

    def ctrl_inference(self):
        # 创建上传目录
        os.makedirs(f"{os.getcwd()}/uploads", exist_ok=True)

        # 接收并保存文件
        file = request.files['file']
        filetype = file.filename.split(".")[-1]
        filename = time.strftime("%Y%m%d-%H%M%S.") + filetype
        path = f"{os.getcwd()}/uploads/{filename}"
        file.save(path)
        self.logger.info(f"File saved to {path}")

        result = self.do_inference(path, filename)
        return result

    def ctrl_inference_binary(self):
        os.makedirs(f"{os.getcwd()}/uploads", exist_ok=True)
        arg_filename = request.args.get("filename")

        self.logger.info(f"Received request: Method={request.method}, Content-Type={request.content_type}")
        self.logger.info(f"Request headers: {request.headers}")

        # 获取字符串形式的图像数据
        image_data_str = request.data.decode('utf-8')
        self.logger.info(f"Received data: {image_data_str[:100]}...")  # 记录前100个字符

        # 将字符串转换回字节数组
        image_data = bytes(map(int, filter(None, image_data_str.split(','))))
        
        self.logger.info(f"Converted data length: {len(image_data)} bytes")
        self.logger.info(f"First few bytes: {image_data[:20].hex()}")

        # 尝试检测图像格式
        image = Image.open(io.BytesIO(image_data))
        self.logger.info(f"Detected image format: {image.format}")
        self.logger.info(f"Created image with size: {image.size}")

        # 保存图像
        file_ext = arg_filename.split(".")[-1]
        filename = time.strftime("%Y%m%d-%H%M%S.") +  file_ext
        path = f"{os.getcwd()}/uploads/{filename}"
        image.save(path)
        self.logger.info("Image saved successfully")

        # 调用推理方法
        result = self.do_inference(path, filename)
        return result

   
    def ctrl_inference_test(self):
        os.makedirs(f"{os.getcwd()}/uploads", exist_ok=True)
        # Get the file string from the request
        fileStr = request.form["file"]
        result = {
            "filestr": fileStr
        }
        return result
    
    
    def ctrl_get_all_histories(self):
        page = request.args.get("page")
        limit = request.args.get("limit")
        date_string = request.args.get("date")
        result = PostgresModel().get_all_histories(page,limit,date_string)
        resp = []
        for row in result:
            try:
                resp.append({
                    "image_id": row[0],
                    "detected_flaws": json.loads(row[1])["result"]["detection_boxes"].__len__()
                    
                })
            except:
                resp.append({
                    "image_id": row[0],
                    "detected_flaws": 0
                })
        return {
            "success": True,
            "result": resp
        }
    
    def ctrl_get_one_history_result(self):
        image_id = request.args.get("image_id")
        result = PostgresModel().get_inference_json(image_id)
        if result is None:
            return {
                "success": False,
                "error_code": 404,
                "error_msg": f"Image ID {image_id} not found"
            }
        return {
            "success": True,
            "result": {
                "image_id": image_id,
                "inference_result": result
            }
        }
    
