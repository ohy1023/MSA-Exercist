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
class Test(db.Model):
    __tablename__ = "test"
    name = db.Column(db.String(10), primary_key=True)
    gender = db.Column(db.String(10))
    phone = db.Column(db.String(50))

    def __init__(self, name, gender, phone):
        self.name = name
        self.gender = gender
        self.phone = phone

@app.route('/faces/test', methods=['GET'])
def test():
    return "test 성공"

# 엔드포인트 추가: 데이터베이스에 데이터 추가 테스트
@app.route('/faces/add_face', methods=['POST'])
def add_user():
    new_user = Test(name="John1", gender="Male", phone="1234567890")
    db.session.add(new_user)
    db.session.commit()
    return jsonify({"message": "User added successfully"}), 201

# 엔드포인트 추가: 데이터베이스에서 사용자 조회
@app.route('/faces/get_users', methods=['GET'])
def get_users():
    users = Test.query.all()
    return jsonify([{"name": user.name, "gender": user.gender, "phone": user.phone} for user in users])

if __name__ == '__main__':
    # Flask 앱 실행 시에만 RabbitMQ 리스너 스레드를 시작
    listener_thread = threading.Thread(target=start_rabbitmq_listener)
    listener_thread.daemon = True
    listener_thread.start()

    with app.app_context():
        db.create_all()  # 테이블 생성

    app.run(host='127.0.0.1', port=5000, debug=True)
