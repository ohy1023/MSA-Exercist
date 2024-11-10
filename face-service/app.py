from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from config import load_config_from_server
import py_eureka_client.eureka_client as eureka_client
import pika
import threading

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
    vector = db.Column(db.Text, nullable=True)
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

# 엔드포인트 추가: 데이터베이스에 데이터 추가 테스트
@app.route('/faces/add_face', methods=['POST'])
def add_user():
    new_user = Face(vector="John1efeafdfae", face_img="Male", ticket_id=1, event_date_id=17700020)
    db.session.add(new_user)
    db.session.commit()
    return jsonify({"message": "User added successfully"}), 201

# 엔드포인트 추가: 데이터베이스에서 사용자 조회
@app.route('/faces/get_faces', methods=['GET'])
def get_users():
    faces = Face.query.all()
    return jsonify([{"face_id": face.face_id, "vector": face.vector, "face_img": face.face_img, "ticke_id": face.ticket_id, "event_date_id":face.event_date_id} for face in faces])

if __name__ == '__main__':
    # Flask 앱 실행 시에만 RabbitMQ 리스너 스레드를 시작
    listener_thread = threading.Thread(target=start_rabbitmq_listener)
    listener_thread.daemon = True
    listener_thread.start()

    with app.app_context():
        db.create_all()  # 테이블 생성

    app.run(host='127.0.0.1', port=5000, debug=True)
