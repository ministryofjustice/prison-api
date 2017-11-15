/* Default security group */
resource "aws_security_group" "db_demo_group" {
  name = "db-demo-group"
  description = "Default security group that allows inbound and outbound traffic from all instances in the VPC"

  ingress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    self        = true
  }

  ingress {
    from_port = 1521
    to_port   = 1521
    protocol  = "tcp"
    cidr_blocks = ["217.33.148.210/32", "81.97.61.151/32"]
  }

  egress {
    from_port   = "0"
    to_port     = "0"
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    self        = true
  }
  egress {
    from_port = 1521
    to_port   = 1521
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags { 
    Name = "db-sg"
  }
}