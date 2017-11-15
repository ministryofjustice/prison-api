output "master.ip" {
  value = "${aws_db_instance.syscon-test-db.address}"
}