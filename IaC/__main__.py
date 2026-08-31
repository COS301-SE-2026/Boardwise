"""A Python Pulumi program"""

import json
import pulumi
import pulumi_aws as aws
import pulumi_cloudflare as cloudflare
import pulumi_docker as docker


RESOURCE_PREFIX = "boardwise"

# --- Set up budget, budget alerts and cost anomaly
# Budget to measure how much of our credits are being used
budget = aws.budgets.Budget(
    f"{RESOURCE_PREFIX}-costs",
    name=f"{RESOURCE_PREFIX}-costs",
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
    f"{RESOURCE_PREFIX}-anomalies",
    monitor_type="DIMENSIONAL",
    monitor_dimension="SERVICE"
)

# Who to notify when anomalies occur
anomaly_subs = aws.costexplorer.AnomalySubscription(
    f"{RESOURCE_PREFIX}-anomaly-alerts",
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

# --- SET UP REVERSE PROXY
# set up Virtual Private Cloud (Basically private network, this will allow our EC2 Instances to communicate only amongst each other and others... )
vpc = aws.ec2.Vpc(
    f"{RESOURCE_PREFIX}-vpc",
    cidr_block="10.0.0.0/16",
    enable_dns_hostnames=True,
    enable_dns_support=True,
    tags={"Name": f"{RESOURCE_PREFIX}-vpc"}
)

igw = aws.ec2.InternetGateway(
    f"{RESOURCE_PREFIX}-vpc-igw",
    vpc_id=vpc.id,
    tags={"Name": f"{RESOURCE_PREFIX}-vpc-igw"}
)

route_table = aws.ec2.RouteTable(
    f"{RESOURCE_PREFIX}-public-route-table",
    vpc_id=vpc.id,
    routes=[
        aws.ec2.RouteTableRouteArgs(
            cidr_block="0.0.0.0/0",
            gateway_id=igw.id
        )
    ],
    tags={"Name": f"{RESOURCE_PREFIX}-public-route-table"}
)

azs = aws.get_availability_zones(state="available")
public_subnets = list()
private_subnets = list()

for i in range(2):
    az = azs.names[i]

    public_subnet = aws.ec2.Subnet(
        f"boardwise-public-subnet-{i + 1}",
        vpc_id=vpc.id,
        cidr_block=f"10.0.{i}.0/24",
        availability_zone=az,
        map_public_ip_on_launch=True,
        tags={"Name": f"boardwise-public-subnet-{i + 1}"}
    )

    rta = aws.ec2.RouteTableAssociation(
        f"boardwise-public-rta-{i + 1}",
        subnet_id=public_subnet.id,
        route_table_id=route_table.id
    )

    public_subnets.append(public_subnet)

    private_subnet = aws.ec2.Subnet(
        f"boardwise-private-subnet-{i + 1}",
        vpc_id=vpc.id,
        cidr_block=f"10.0.{i + 10}.0/24",
        availability_zone=az,
        tags={"Name": f"boardwise-private-subnet-{i + 1}"}
    )
    private_subnets.append(private_subnet)

# Essential a "fire wall", define how what traffic is accepted and that (COS 332)
caddy_sg = aws.ec2.SecurityGroup(
    f"{RESOURCE_PREFIX}-caddy-sg",
    description="Allow HTTP & HTTPS traffic",
    vpc_id=vpc.id,
)

caddy_ingress_http = aws.vpc.SecurityGroupIngressRule(
    "caddy-ingress-http",
    security_group_id=caddy_sg.id,
    cidr_ipv4="0.0.0.0/0",
    from_port=80,
    to_port=80,
    ip_protocol="tcp"
) 

caddy_ingress_icmp = aws.vpc.SecurityGroupIngressRule(
    "caddy-ingress-icmp",
    security_group_id=caddy_sg.id,
    cidr_ipv4="0.0.0.0/0",
    from_port=8,
    to_port=0,
    ip_protocol="icmp"
)

caddy_ingress_https = aws.vpc.SecurityGroupIngressRule(
    "caddy-ingress-https",
    security_group_id=caddy_sg.id,
    cidr_ipv4="0.0.0.0/0",
    from_port=443,
    to_port=443,
    ip_protocol="tcp"
)

caddy_egress_ipv4 = aws.vpc.SecurityGroupEgressRule(
    "caddy-egress-ipv4",
    security_group_id=caddy_sg.id,
    cidr_ipv4="0.0.0.0/0",
    ip_protocol="-1"
)

caddy_egress_ipv6 = aws.vpc.SecurityGroupEgressRule(
    "caddy-egress-ipv6",
    security_group_id=caddy_sg.id,
    cidr_ipv6="::/0",
    ip_protocol="-1"
)

# Set up ecs &-ec2 instance for caddy
ami = aws.ssm.get_parameter(
    name="/aws/service/ecs/optimized-ami/amazon-linux-2023/recommended/image_id"
)

setup_script = r"""#!bin/bash
systemctl enable --now docker

mkdir -p /etc/caddy
cat << 'EOF' > /etc/caddy/Caddyfile
:80 {
    respond "Boardwise Caddy reverse proxy online" 200
}
EOF

docker run -d \
    --name caddy \
    --restart always \
    -p 80:80 \
    -p 443:443 \
    -v /etc/caddy/Caddyfile:/etc/caddy/Caddyfile \
    -v caddy_data:/data \
    -v caddy_config:/config \
    caddy:2.11.4-alpine
"""

caddy_instance = aws.ec2.Instance(
    "boardwise-reverse-proxy",
    instance_type="t3.micro",
    ami=ami.value,
    vpc_security_group_ids=[caddy_sg.id],
    subnet_id=public_subnets[0].id,
    user_data=setup_script,
    tags={"Name": "boardwise-reverse-proxy"}
)

# TODO: Possibly add an Elastic IP


# set backend security groups
spring_sg = aws.ec2.SecurityGroup(
    "boardwise-spring-sg",
    description="only permit traffic from Caddy instance and allow spring backend outgoing traffic",
    vpc_id=vpc.id
)

caddy_to_spring = aws.vpc.SecurityGroupIngressRule(
    "spring-sg-ingress",
    description="Permit traffic from reverse proxy for spring backend",
    security_group_id=spring_sg.id,
    referenced_security_group_id=caddy_sg.id,
    from_port=8080,
    to_port=8080,
    ip_protocol="tcp"
)

spring_egress_ipv6 = aws.vpc.SecurityGroupEgressRule(
    "spring-sg-egress-ipv6",
    description="to allow spring backend to make requests to the outside [IPv6]",
    security_group_id=spring_sg.id,
    cidr_ipv4="::/0",
    ip_protocol="-1"
)

spring_egress_ipv4 = aws.vpc.SecurityGroupEgressRule(
    "spring-sg-egress-ipv4",
    description="to allow spring backend to make requests to the outside [IPv4]",
    security_group_id=spring_sg.id,
    cidr_ipv4="0.0.0.0/0",
    ip_protocol="-1"
)

python_sg = aws.ec2.SecurityGroup(
    "boardwise-python-sg",
    description="Only permit traffic from Caddy instance and Spring boot",
    vpc_id=vpc.id
)

caddy_to_python = aws.vpc.SecurityGroupIngressRule(
    "python-sg-ingress-caddy",
    description="Permit traffic from reverse proxy to python/fastapi backend",
    security_group_id=python_sg.id,
    referenced_security_group_id=caddy_sg.id,
    from_port=8000,
    to_port=8000,
    ip_protocol="tcp"
)

spring_to_python = aws.vpc.SecurityGroupIngressRule(
    "python-sg-ingress-spring",
    description="Permit traffic from spring backend to python/fastapi backend",
    security_group_id=python_sg.id,
    referenced_security_group_id=spring_sg.id,
    from_port=8000,
    to_port=8000,
    ip_protocol="tcp"
)

python_egress_ipv6 = aws.vpc.SecurityGroupEgressRule(
    "python-sg-egress-ipv6",
    description="to allow python backend to make requests to the outside [IPv6]",
    security_group_id=python_sg.id,
    cidr_ipv4="::/0",
    ip_protocol="-1"
)

python_egress_ipv4 = aws.vpc.SecurityGroupEgressRule(
    "python-sg-egress-ipv4",
    description="to allow python backend to make requests to the outside [IPv4]",
    security_group_id=python_sg.id,
    cidr_ipv4="0.0.0.0/0",
    ip_protocol="-1"
)






pulumi.export("public_subnets",  [subnet.tags["Name"] for subnet in public_subnets])
pulumi.export("private_subnets", [subnet.tags["Name"] for subnet in private_subnets])