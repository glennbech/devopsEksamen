resource "aws_cloudwatch_metric_alarm" "threshold" {
  alarm_name  = "${var.prefix}-greater-threshold"
  namespace   = var.prefix
  metric_name = var.metric_name

  comparison_operator = "GreaterThanThreshold"
  threshold           = var.threshold
  evaluation_periods  = "2"
  period              = "60"
  statistic           = "Maximum"

  alarm_description = "Alarm Module is for ${var.metric_name} metric and has alarm set too ${var.threshold} GreaterThanThreshold"
  alarm_actions     = [aws_sns_topic.user_updates.arn]
}

resource "aws_sns_topic" "user_updates" {
  name = "${var.prefix}-alarm-topic"
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  topic_arn = aws_sns_topic.user_updates.arn
  protocol  = "email"
  endpoint  = var.email
}