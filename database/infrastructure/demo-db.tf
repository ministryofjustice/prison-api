
/* Setup our aws provider */
provider "aws" {
  region                  = "${var.region}"
  shared_credentials_file = "~/.aws/credentials"
  profile                 = "syscon"
}

resource "aws_db_instance" "syscon-test-db" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "oracle-se2"
  engine_version       = "12.1.0.2.v9"
  instance_class       = "db.t2.micro"
  name                 = "testdb"
  username             = "SYSCON_TEST"
  password             = "${var.database_password}"
  parameter_group_name = "default.oracle-se2-12.1"
  vpc_security_group_ids = [ "${aws_security_group.db_demo_group.id}" ]
  character_set_name   = "AL32UTF8"
  publicly_accessible  = "true"
  license_model        = "license-included"
  skip_final_snapshot  = "true"
}