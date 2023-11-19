terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.25.0"
    }
  }


  backend "s3" {
    bucket = "kandidat-id-2012"
    key    = "kandidat-id-2012/apprunner-a-new-state.state"
    region = "eu-west-1"
  }

}

