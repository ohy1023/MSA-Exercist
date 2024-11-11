import py_eureka_client.eureka_client as eureka_client
import pika
import threading
import numpy as np
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from config import load_config_from_server
from face_utils import get_face_embedding, cosine_similarity
from vector_security_utils import encrypt_vector, decrypt_vector

app = Flask(__name__)

# 초기 설정 로드
config = load_config_from_server()

# DB 설정
db_url = config["mysql"].get("url")
db_password = config["mysql"].get("password")

# SQLAlchemy Database URI 설정
if db_url and db_password:
    app.config['SQLALCHEMY_DATABASE_URI'] = db_url.replace("{password}", db_password)
else:
    print("Failed to load database configuration")

# DB 객체 생성
db = SQLAlchemy(app)

# RabbitMQ 메시지 수신 설정
def start_rabbitmq_listener():
    RABBITMQ_HOST = 'localhost'
    QUEUE_NAME = 'springCloudBus'
    credentials = pika.PlainCredentials(
        username='guest',
        password='guest'
    )

    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials))
    channel = connection.channel()

    channel.exchange_declare(exchange=QUEUE_NAME, exchange_type='topic', passive=True)

    channel.queue_declare(queue='springCloudBusByFlask')
    channel.queue_bind(exchange=QUEUE_NAME, queue='springCloudBusByFlask', routing_key='#')

    def callback(ch, method, properties, body):
        print("Received configuration update message from RabbitMQ")
        # 최신 설정 다시 로드
        global config, db_url, db_password
        config = load_config_from_server()
        db_url = config["mysql"].get("url")
        db_password = config["mysql"].get("password")
        # 데이터베이스 URI 업데이트
        if db_url and db_password:
            app.config['SQLALCHEMY_DATABASE_URI'] = db_url.replace("{password}", db_password)

    channel.basic_consume(queue='springCloudBusByFlask', on_message_callback=callback, auto_ack=True)
    print("Waiting for configuration updates...")
    channel.start_consuming()

# Eureka Client 초기화
eureka_client.init(eureka_server="http://localhost:8761/eureka",
                   app_name="face-service",
                   instance_host="localhost",
                   instance_port=5000
                   )

# 모델 정의
class Face(db.Model):
    __tablename__ = "face"
    face_id = db.Column(db.BigInteger, primary_key=True, autoincrement=True, nullable=False)
    vector = db.Column(db.Text, nullable=True)  # 암호화된 문자열로 저장
    face_img = db.Column(db.String(255), nullable=True)
    ticket_id = db.Column(db.BigInteger, nullable=True)
    event_date_id = db.Column(db.BigInteger, nullable=True)

    def __init__(self, vector, face_img, ticket_id, event_date_id):
        self.vector = vector
        self.face_img = face_img
        self.ticket_id = ticket_id
        self.event_date_id = event_date_id

@app.route('/faces/test', methods=['GET'])
def test():
    return "test 성공"

# 얼굴 벡터를 DB에 저장하는 엔드포인트
@app.route('/faces/upload', methods=['POST'])
def upload_face():
    # multipart/form-data 형식에서 파일과 추가 데이터를 수신
    file = request.files.get('file')
    ticket_id = request.form.get('ticket_id')
    event_date_id = request.form.get('event_date_id')

    if not file:
        return jsonify({"error": "No file part in the request"}), 400

    # 얼굴 벡터 추출
    image_data = file.read()
    embedding = get_face_embedding(image_data)
    if embedding is None:
        return jsonify({"error": "No face detected"}), 400

    # 벡터 암호화
    encrypted_embedding = encrypt_vector(embedding)

    # DB 저장
    new_face = Face(vector=encrypted_embedding, face_img="s3url", ticket_id=ticket_id, event_date_id=event_date_id)
    db.session.add(new_face)
    db.session.commit()

    return jsonify({"message": "Face embedding extracted and stored successfully"}), 200

# 입력 얼굴과 DB 얼굴 벡터 비교 엔드포인트
@app.route('/faces/match', methods=['POST'])
def match_face():
    file = request.files.get('file')
    event_date_id = request.form.get('event_date_id')

    if not file or not event_date_id:
        return jsonify({"error": "File or event_date_id missing"}), 400

    image_data = file.read()
    embedding = get_face_embedding(image_data)
    if embedding is None:
        return jsonify({"error": "No face detected"}), 400

    # event_date_id가 일치하는 Face 데이터를 조회
    faces = Face.query.filter_by(event_date_id=event_date_id).all()

    if not faces:
        return jsonify({"error": "No faces found for the given event_date_id"}), 404
    
    max_similarity = -1
    best_match = None

    for face in faces:
        # DB에서 암호화된 벡터를 복호화하여 NumPy 배열로 변환
        decrypted_embedding = decrypt_vector(face.vector)
        similarity = cosine_similarity(embedding, decrypted_embedding)

        if similarity > max_similarity:
            max_similarity = similarity
            best_match = {
                "face_id": face.face_id,
                "face_img": face.face_img,
                "ticket_id": face.ticket_id,
                "event_date_id": face.event_date_id,
                "similarity": float(similarity)
            }

    # 임계값 설정
    threshold = 0.4
    if best_match and max_similarity > threshold:
        return jsonify({"match": best_match}), 200
    else:
        return jsonify({"message": "No matching face found"}), 404

if __name__ == '__main__':
    # Flask 앱 실행 시에만 RabbitMQ 리스너 스레드를 시작
    listener_thread = threading.Thread(target=start_rabbitmq_listener)
    listener_thread.daemon = True
    listener_thread.start()

    with app.app_context():
        db.create_all()  # 테이블 생성

    app.run(host='127.0.0.1', port=5000, debug=True)
