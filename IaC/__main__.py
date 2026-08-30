"""A Python Pulumi program"""

import pulumi
import pulumi_aws as aws
import pulumi_cloudflare as cloudflare

# Set up budget, budget alerts and cost anomaly
# Budget to measure how much of our credits are being used
budget = aws.budgets.Budget(
    "boardwise-costs",
    budget_type="COST",
    limit_amount="200",
    limit_unit="USD",
    time_unit="ANNUALLY",
    notifications=[
        { # 50 % 
            "comparison_operator": "GREATER_THAN",
            "threshold": float(50),
            "threshold_type": "PERCENTAGE",
            "notification_type": "ACTUAL",
            "subscriber_email_addresses": [
                "worksonmymachine67@gmail.com"
            ]
        },
        { # 75 %
            "comparison_operator": "GREATER_THAN",
            "threshold": float(75),
            "threshold_type": "PERCENTAGE",
            "notification_type": "ACTUAL",
            "subscriber_email_addresses": [
                "worksonmymachine67@gmail.com"
            ]
        },
        { # 90 %
            "comparison_operator": "GREATER_THAN",
            "threshold": float(90),
            "threshold_type": "PERCENTAGE",
            "notification_type": "FORECASTED",
            "subscriber_email_addresses": [
                "worksonmymachine67@gmail.com"
            ]
        },
        { # 100 % (when depleted)
            "comparison_operator": "EQUAL_TO",
            "threshold": float(100),
            "threshold_type": "PERCENTAGE",
            "notification_type": "ACTUAL",
            "subscriber_email_addresses": [
                "worksonmymachine67@gmail.com"
            ]
        }
    ]
)

# Anomaly monitor to catch potential spikes in our expenditure
anomaly_monitor = aws.costexplorer.AnomalyMonitor(
    "boardwise-anomalies",
    monitor_type="DIMENSIONAL",
    monitor_dimension="SERVICE"
)

# Who to notify when anomalies occur
anomaly_subs = aws.costexplorer.AnomalySubscription(
    "boardwise-anomaly-alerts",
    frequency="DAILY",
    monitor_arn_lists=[anomaly_monitor.arn],
    subscribers=[{
        "type": "EMAIL",
        "address": "worksonmymachine67@gmail.com"
    }],
    threshold_expression={
        "dimension": {
            "key": "ANOMALY_TOTAL_IMPACT_ABSOLUTE",
            "match_options": ["GREATER_THAN_OR_EQUAL"],
            "values": ["10.00"] # if anomalies cause a >=$10 spike
        }
    }
)

# --- SETUP Certificates ---

