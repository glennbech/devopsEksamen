variable "image" {
  type = string
}
variable "port" {
  type = string
  default = "8080"
}

variable "prefix" {
  type = string

}
variable "threshold" {
  default = "2"
  type = string
}

variable "email" {
  type = string

}
variable "metric_name" {
  type = string

}

variable "BUCKET_NAME" {
  type = string

}