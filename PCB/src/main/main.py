from dotenv import load_dotenv
# from pathlib import Path
from flask_cors import CORS
import logging
# import time
from ws_server import WsServer
from flask import Flask, send_from_directory
import sys
import os

from router.inference_bp import inference_bp
from router.iot_bp import iot_bp
# import util.iot as iot_util


load_dotenv(verbose=True)

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/..")

# add all submodules to sys.path
sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/../controller")
sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/../router")
sys.path.append(os.path.dirname(os.path.abspath(__file__)) + "/../util")

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

app = Flask(__name__)
cors = CORS(app)

app.config['STATIC_FOLDER'] = os.path.join(os.getcwd(), 'uploads')
app.register_blueprint(inference_bp)
app.register_blueprint(iot_bp)

@app.route('/images/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['STATIC_FOLDER'],filename)

@app.route('/hls/<filename>')
def hls(filename):
    return send_from_directory(app.config['STATIC_FOLDER']+"/hls",filename)

if __name__ == "__main__":
    os.makedirs(app.config['STATIC_FOLDER'], exist_ok=True)
    os.makedirs(app.config['STATIC_FOLDER']+"/hls", exist_ok=True)
    ws_server = WsServer()
    ws_server.start()
    app.run(host="0.0.0.0"
            , port=5001
            , debug=True)


