terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.25.0"
    }
  }


  backend "s3" {
    bucket = "pgr301-sam-bucket"
    key    = "kandidat-id-2012/apprunner-a-new-state.state"
    region = "eu-west-1"
  }

}

