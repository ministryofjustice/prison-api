#!/usr/bin/env bash
aws --endpoint-url=http://localhost:4575 sns create-topic --name offender_events
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name elite2_api_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name elite2_api_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/elite2_api_queue --attributes file://set-queue-attributes.json
aws --endpoint-url=http://localhost:4575 sns subscribe \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:offender_events \
    --protocol sqs \
    --notification-endpoint http://localhost:4576/queue/elite2_api_queue \
    --attributes '{"FilterPolicy":"{\"eventType\":[\"DATA_COMPLIANCE_DELETE-OFFENDER\"]}"}'
