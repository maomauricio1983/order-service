import json
import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def handler(event, context):
    logger.info(f"Recibidos {len(event['Records'])} mensajes de SQS")

    for record in event['Records']:
        body = json.loads(record['body'])
        logger.info(f"Procesando orden: id={body.get('orderId')}, cliente={body.get('customerId')}, total={body.get('totalAmount')}")

    return {"statusCode": 200, "processed": len(event['Records'])}
