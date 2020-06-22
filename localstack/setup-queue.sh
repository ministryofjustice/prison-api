#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID=anykey
export AWS_SECRET_ACCESS_KEY=anysecret
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name data_compliance_response_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name data_compliance_response_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/data_compliance_response_queue --attributes file:///docker-entrypoint-initaws.d/data-compliance-response-queue-attributes.json

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name data_compliance_request_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name data_compliance_request_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/data_compliance_request_queue --attributes file:///docker-entrypoint-initaws.d/data-compliance-request-queue-attributes.json

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name delete_offender_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name delete_offender_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/delete_offender_queue --attributes file:///docker-entrypoint-initaws.d/delete-offender-queue-attributes.json

aws --endpoint-url=http://localhost:4575 sns create-topic --name offender_events
aws --endpoint-url=http://localhost:4575 sns subscribe \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:offender_events \
    --protocol sqs \
    --notification-endpoint http://localhost:4576/queue/delete_offender_queue \
    --attributes '{"FilterPolicy":"{\"eventType\":[\"DATA_COMPLIANCE_DELETE-OFFENDER\"]}"}'
