resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.prefix
  dashboard_body = <<DASHBOARD
{
 "widgets": [
        {
            "height": 6,
            "width": 18,
            "y": 0,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "student2012", "customer_count.value", { "region": "eu-west-1", "label": "Cutomer visiting" } ],
                    [ ".", "employee_count.value", { "region": "eu-west-1", "label": "Employees at work" } ],
                    [ ".", "person_count.value", { "region": "eu-west-1", "label": "Total persons in building" } ]
                ],
                "view": "gauge",
                "region": "eu-west-1",
                "yAxis": {
                    "left": {
                        "min": 0,
                        "max": 10
                    }
                },
                "period": 1,
                "stat": "Average",
                "title": "Current persons in building"
            }
        },
        {
            "height": 7,
            "width": 18,
            "y": 6,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "student2012", "user_tracking.count", { "region": "eu-west-1", "visible": false } ],
                    [ ".", "user_tracking.sum", { "region": "eu-west-1" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "eu-west-1",
                "period": 60,
                "stat": "Sum",
                "title": "Time spen in building",
                "setPeriodToTimeRange": false,
                "sparkline": true,
                "trend": true,
                "liveData": false,
                "start": "-PT1H",
                "end": "P0D"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 0,
            "x": 18,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "student2012", "unauthorized_scan_attempts.count", { "region": "eu-west-1", "label": "unauthorized etempts" } ]
                ],
                "sparkline": false,
                "view": "singleValue",
                "region": "eu-west-1",
                "liveData": true,
                "period": 300,
                "stat": "Sum",
                "title": "Unatoriced atempts"
            }
        },
        {
            "height": 3,
            "width": 6,
            "y": 6,
            "x": 18,
            "type": "alarm",
            "properties": {
                "title": "",
                "alarms": [
                    "arn:aws:cloudwatch:eu-west-1:244530008913:alarm:To many unathoriced atempts"
                ]
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 13,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "student2012", "method.timed.avg", "exception", "none", "method", "scanPrivateEntranceAutomatic", "class", "com.example.s3rekognition.controller.CameraController", { "region": "eu-west-1", "visible": false } ],
                    [ ".", "method.timed.max", ".", ".", ".", ".", ".", ".", { "region": "eu-west-1", "label": "CameraController Auto Scan Private Timer" } ],
                    [ ".", "method.timed.sum", ".", ".", ".", ".", ".", ".", { "region": "eu-west-1", "visible": false } ],
                    [ ".", "method.timed.max", ".", ".", ".", "scanForPPE", ".", "com.example.s3rekognition.controller.RekognitionController", { "region": "eu-west-1", "label": "RekognitionController scanForPPE Timer" } ]
                ],
                "sparkline": true,
                "view": "bar",
                "region": "eu-west-1",
                "period": 3600,
                "stat": "Maximum",
                "setPeriodToTimeRange": true,
                "title": "Controller timer"
            }
        },
        {
            "height": 7,
            "width": 12,
            "y": 13,
            "x": 6,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "student2012", "get_image_timer.sum", { "region": "eu-west-1", "label": "create image Timer", "visible": false } ],
                    [ ".", "get_matching_faces_timer.sum", { "region": "eu-west-1", "label": "Create Matching Faces Timer", "visible": false } ],
                    [ ".", "Image_loop_timer.sum", { "region": "eu-west-1", "label": "Image Loop Timer", "visible": false } ],
                    [ ".", "image_retrive_timer.sum", { "region": "eu-west-1", "label": "Load image from S3 Timer", "visible": false } ],
                    [ ".", "scan_image_at_private_entrance_timer.sum", { "region": "eu-west-1", "label": "Scan Image Timer", "visible": false } ],
                    [ ".", "get_image_timer.avg", { "region": "eu-west-1", "label": "create Image Timer" } ],
                    [ ".", "get_matching_faces_timer.avg", { "region": "eu-west-1", "label": "Create matching faces Timer" } ],
                    [ ".", "image_retrive_timer.avg", { "region": "eu-west-1", "label": "Get Image From S3 Timer" } ],
                    [ ".", "scan_image_at_private_entrance_timer.avg", { "region": "eu-west-1", "label": "Sacn Image Timer" } ]
                ],
                "view": "bar",
                "region": "eu-west-1",
                "period": 300,
                "stat": "Average",
                "title": "Method time use in CameraController",
                "liveData": true
            }
        }

  ]
}
DASHBOARD
}