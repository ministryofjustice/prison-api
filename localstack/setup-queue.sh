#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID=anykey
export AWS_SECRET_ACCESS_KEY=anysecret
export AWS_DEFAULT_REGION=eu-west-2

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name outbound_referral_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name outbound_referral_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/outbound_referral_queue --attributes file:///docker-entrypoint-initaws.d/outbound-referral-queue-attributes.json

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name inbound_deletion_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name inbound_deletion_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/inbound_deletion_queue --attributes file:///docker-entrypoint-initaws.d/inbound-deletion-queue-attributes.json

aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name outbound_deletion_queue
aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name outbound_deletion_dead_letter_queue
aws --endpoint-url=http://localhost:4576 sqs set-queue-attributes \
    --queue-url http://localhost:4576/queue/outbound_deletion_queue --attributes file:///docker-entrypoint-initaws.d/outbound-deletion-queue-attributes.json

aws --endpoint-url=http://localhost:4575 sns create-topic --name offender_events
aws --endpoint-url=http://localhost:4575 sns subscribe \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:offender_events \
    --protocol sqs \
    --notification-endpoint http://localhost:4576/queue/outbound_deletion_queue \
    --attributes '{"FilterPolicy":"{\"eventType\":[\"DATA_COMPLIANCE_DELETE-OFFENDER\"]}"}'
