# 日志
## 2
iot.py
- 增加无法连接时的处理

others
- 整理文件格式
## 1
inference_controller.py:
- 创建函数do_inference，复用推理部分代码
- 删除old_inference，它未被使用也无法正常运行
- 注释和log改进

postgres.py
- 更新了`get_all_histories`方法来处理date_string或limit为None的情形，防止出现错误
- 处理了`get_inference_json`未找到记录的情况，避免报错

inference_local_v9.py
- 删除未使用的import和无用的replace

new
- 在`test.md`中添加了详细的测试命令，用于推理和历史检索。