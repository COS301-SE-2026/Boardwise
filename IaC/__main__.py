"""A Python Pulumi program"""

import json
import pulumi
import os
import mimetypes
from config import settings
import pulumi_aws as aws
import pulumi_cloudflare as cloudflare
import pulumi_awsx as awsx

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

# --- SET UP Security groups Essentially a "fire wall", define how what traffic is accepted and that (COS 332)
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
    cidr_ipv6="::/0",
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
    cidr_ipv6="::/0",
    ip_protocol="-1"
)

python_egress_ipv4 = aws.vpc.SecurityGroupEgressRule(
    "python-sg-egress-ipv4",
    description="to allow python backend to make requests to the outside [IPv4]",
    security_group_id=python_sg.id,
    cidr_ipv4="0.0.0.0/0",
    ip_protocol="-1"
)

# set up backend
ami = aws.ssm.get_parameter(
    name="/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
)

backend_role = aws.iam.Role(
    f"{RESOURCE_PREFIX}-backend-role",
    assume_role_policy=json.dumps({
        "Version": "2012-10-17",
        "Statement": [
            {
                "Action": "sts:AssumeRole",
                "Effect": "Allow",
                "Principal": {
                    "Service": "ec2.amazonaws.com"
                }
            }
        ]
    })
)

rpa_ecr = aws.iam.RolePolicyAttachment(
    f"{RESOURCE_PREFIX}-ecr-policy",
    role=backend_role.name,
    policy_arn="arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
)

rpa_ssm = aws.iam.RolePolicyAttachment(
    f"{RESOURCE_PREFIX}-ssm-policy",
    role=backend_role.name,
    policy_arn="arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
)

backend_profile = aws.iam.InstanceProfile(
    f"{RESOURCE_PREFIX}-backend-profile",
    role=backend_role.name
)

python_repo = awsx.ecr.Repository(f"{RESOURCE_PREFIX}-python-repo", force_delete=True)

python_image = awsx.ecr.Image(
    f"{RESOURCE_PREFIX}-python-image",
    repository_url=python_repo.url,
    context="../ai",
    platform="linux/amd64"
)

python_setup_script = r"""#!/bin/bash
yum update -y
yum install -y docker

systemctl enable --now docker

aws ecr get-login-password --region __REGION__ | docker login --username AWS --password-stdin __REGISTRY_URL__

docker run -d \
    --restart always \
    --name ai-backend \
    -p 8000:8000 \
    -e PROD_DB_URL="__PROD_DB_URL__" \
    -e DB_NAME="__DB_NAME__" \
    -e JWT_SECRET="__JWT_SECRET__" \
    -e JWT_ALGORITHM="__JWT_ALGORITHM__" \
    -e R2_ACCOUNT_ID="__R2_ACCOUNT_ID__" \
    -e R2_BUCKET_RULEBOOKS="__R2_BUCKET_RULEBOOKS__" \
    -e R2_ACCESS_KEY="__R2_ACCESS_KEY__" \
    -e R2_SECRET_KEY="__R2_SECRET_KEY__" \
    -e HF_TOKEN="__HF_TOKEN__" \
    -e INTERNAL_SECRET="__INTERNAL_SECRET__" \
    -e CPU_CORES="__CPU_CORES__" \
    __IMAGE_URI__
"""
python_user_data = python_image.image_uri.apply(
    lambda image_uri : python_setup_script
                        .replace("__IMAGE_URI__", image_uri)
                        .replace("__CPU_CORES__", str(settings.CPU_CORES))
                        .replace("__INTERNAL_SECRET__", settings.INTERNAL_WEBHOOK_SECRET)
                        .replace("__HF_TOKEN__", settings.HF_TOKEN)
                        .replace("__R2_SECRET_KEY__", settings.R2_SECRET_KEY)
                        .replace("__R2_ACCESS_KEY__", settings.R2_ACCESS_KEY)
                        .replace("__R2_BUCKET_RULEBOOKS__", settings.R2_BUCKET_RULEBOOKS)
                        .replace("__R2_ACCOUNT_ID__", settings.R2_ACCOUNT_ID)
                        .replace("__JWT_ALGORITHM__", settings.JWT_ALGORITHM)
                        .replace("__JWT_SECRET__", settings.JWT_SECRET)
                        .replace("__DB_NAME__", settings.MONGODB_DATABASE)
                        .replace("__PROD_DB_URL__", settings.MONGODB_URL)
                        .replace("__REGISTRY_URL__", image_uri.split('/')[0])
                        .replace("__REGION__", aws.get_region().id)
)

python_instance = aws.ec2.Instance(
    f"{RESOURCE_PREFIX}-python-backend",
    instance_type="m7i-flex.large",
    ami=ami.value,
    subnet_id=public_subnets[0].id,
    vpc_security_group_ids=[python_sg.id],
    tags={"Name": f"{RESOURCE_PREFIX}-python-backend"},
    user_data=python_user_data,
    iam_instance_profile=backend_profile.name,
    associate_public_ip_address=True
)

spring_repo = awsx.ecr.Repository(f"{RESOURCE_PREFIX}-spring-repo", force_delete=True)
spring_image = awsx.ecr.Image(
    f"{RESOURCE_PREFIX}-spring-image",
    repository_url=spring_repo.url,
    context="../backend",
    platform="linux/amd64"
)

spring_setup_script = r"""#!/bin/bash
yum update -y
yum install -y docker

systemctl enable --now docker

aws ecr get-login-password --region __REGION__ | docker login --username AWS --password-stdin __REGISTRY_URL__

docker run -d \
    --restart always \
    --name spring-backend \
    -p 8080:8080 \
    -e PROD_DB_URL="__PROD_DB_URL__" \
    -e JWT_SECRET="__JWT_SECRET__" \
    -e JWT_ALGORITHM="__JWT_ALGORITHM__" \
    -e R2_ACCOUNT_ID="__R2_ACCOUNT_ID__" \
    -e R2_BUCKET_RULEBOOKS="__R2_BUCKET_RULEBOOKS__" \
    -e R2_ACCESS_KEY="__R2_ACCESS_KEY__" \
    -e R2_SECRET_KEY="__R2_SECRET_KEY__" \
    -e INTERNAL_SECRET="__INTERNAL_SECRET__" \
    -e PROD_FAST_API_BASE="__PROD_FAST_API_BASE__" \
    -e R2_BUCKET_PROFILES="__R2_BUCKET_PROFILES__" \
    -e R2_BUCKET_LISTINGS="__R2_BUCKET_LISTINGS__" \
    -e R2_RULEBOOKS_PUBLIC_PROD_URL="__R2_RULEBOOKS_PUBLIC_PROD_URL__" \
    -e R2_LISTINGS_PROD_ENDPOINT="__R2_LISTINGS_PROD_ENDPOINT__" \
    -e R2_PROD_URL="__R2_PROD_URL__" \
    -e BGG_TOKEN="__BGG_TOKEN__" \
    -e BGG_URL="__BGG_URL__" \
    -e PROD_FRONTEND_BASE="__PROD_FRONTEND_BASE__" \
    -e GOOGLE_MAP_API_KEY="__GOOGLE_MAP_API_KEY__" \
    -e SMTP_HOST="__SMTP_HOST__" \
    -e SMTP_USERNAME="__SMTP_USERNAME__" \
    -e SMTP_PASSWORD="__SMTP_PASSWORD__" \
    __IMAGE_URI__
"""
spring_user_data = pulumi.Output.all(
    image_uri = spring_image.image_uri,
    python_ip = python_instance.private_ip
).apply(
    lambda args : spring_setup_script
                        .replace("__IMAGE_URI__", args["image_uri"])
                        .replace("__SMTP_PASSWORD__", settings.SMTP_PASSWORD if settings.SMTP_PASSWORD is not None else "")
                        .replace("__SMTP_USERNAME__", settings.SMTP_USERNAME if settings.SMTP_USERNAME is not None else "")
                        .replace("__SMTP_HOST__", settings.SMTP_HOST if settings.SMTP_HOST is not None else "")
                        .replace("__GOOGLE_MAP_API_KEY__", settings.GOOGLE_MAP_API_KEY)
                        .replace("__PROD_FRONTEND_BASE__", "https://boardwise.games/")
                        .replace("__BGG_URL__", settings.BGG_URL)
                        .replace("__BGG_TOKEN__", settings.BGG_TOKEN)
                        .replace("__R2_PROD_URL__", settings.R2_PROD_URL)
                        .replace("__R2_LISTINGS_PROD_ENDPOINT__", settings.R2_LISTINGS_PROD_URL)
                        .replace("__R2_RULEBOOKS_PUBLIC_PROD_URL__", settings.R2_RULEBOOKS_PUBLIC_PROD_URL)
                        .replace("__R2_BUCKET_LISTINGS__", settings.R2_BUCKET_LISTINGS)
                        .replace("__R2_BUCKET_PROFILES__", settings.R2_BUCKET_PROFILES)
                        .replace("__PROD_FAST_API_BASE__", f"http://{args["python_ip"]}:8000/api/fa/")
                        .replace("__INTERNAL_SECRET__", settings.INTERNAL_WEBHOOK_SECRET)
                        .replace("__R2_SECRET_KEY__", settings.R2_SECRET_KEY)
                        .replace("__R2_ACCESS_KEY__", settings.R2_ACCESS_KEY)
                        .replace("__R2_BUCKET_RULEBOOKS__", settings.R2_BUCKET_RULEBOOKS)
                        .replace("__R2_ACCOUNT_ID__", settings.R2_ACCOUNT_ID)
                        .replace("__JWT_ALGORITHM__", settings.JWT_ALGORITHM)
                        .replace("__JWT_SECRET__", settings.JWT_SECRET)
                        .replace("__PROD_DB_URL__", settings.MONGODB_URL)
                        .replace("__REGISTRY_URL__", args["image_uri"].split('/')[0])
                        .replace("__REGION__", aws.get_region().id)
)

spring_instance = aws.ec2.Instance(
    f"{RESOURCE_PREFIX}-spring-backend",
    instance_type="m7i-flex.large",
    ami=ami.value,
    subnet_id=public_subnets[0].id,
    vpc_security_group_ids=[spring_sg.id],
    tags={"Name": f"{RESOURCE_PREFIX}-spring-backend"},
    user_data=spring_user_data,
    iam_instance_profile=backend_profile.name,
    associate_public_ip_address=True
)

# Set up ecs &-ec2 instance for caddy

caddy_setup_script = r"""#!/bin/bash
yum update -y
yum install -y docker

systemctl enable --now docker

mkdir -p /etc/caddy
cat << 'EOF' > /etc/caddy/Caddyfile
api.boardwise.games {
    # Route requests to spring
    handle /api/sb/* {
        reverse_proxy __SPRING_IP__:8080
    }

    # Route requests to python
    handle /api/fa/* {
        reverse_proxy __PYTHON_IP__:8000
    }
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

caddy_user_data = pulumi.Output.all(
    spring_ip=spring_instance.private_ip,
    python_ip=python_instance.private_ip
).apply(
    lambda ips: caddy_setup_script
                .replace("__SPRING_IP__", ips["spring_ip"])
                .replace("__PYTHON_IP__", ips["python_ip"])
)

caddy_instance = aws.ec2.Instance(
    "boardwise-reverse-proxy",
    instance_type="t3.micro",
    ami=ami.value,
    vpc_security_group_ids=[caddy_sg.id],
    subnet_id=public_subnets[0].id,
    user_data=caddy_user_data,
    iam_instance_profile=backend_profile.name,
    tags={"Name": "boardwise-reverse-proxy"},
)

caddy_eip = aws.ec2.Eip(
    f"{RESOURCE_PREFIX}-caddy-eip",
    instance=caddy_instance.id,
    domain="vpc",
    tags={"Name": f"{RESOURCE_PREFIX}-caddy-eip"}
)

# set DNS stuff for backend [api.boardwise.games]
rp_record = cloudflare.DnsRecord(
    f"{RESOURCE_PREFIX}-api-dns",
    zone_id=settings.CLOUDFLARE_ZONE_ID,
    name="api",
    type="A",
    proxied=False,
    comment="Route traffic to Caddy (sits in front of backend)",
    content=caddy_eip.public_ip,
    ttl=1
)

# Time set up frontend
bucket = aws.s3.Bucket(
    f"{RESOURCE_PREFIX}-frontend-bucket",
    tags={"Name": f"{RESOURCE_PREFIX}-frontend-bucket"}
)

frontend_build_dir = "../frontend/.output/public"
for root, dirs, files in os.walk(frontend_build_dir):
    for file in files:
        abs_path = os.path.join(root, file)
        rel_path = os.path.relpath(abs_path, frontend_build_dir)
        key = rel_path.replace(os.path.sep, "/")
        mime, _ = mimetypes.guess_type(abs_path)
        mime = mime if mime is not None else "application/octet-stream"

        obj = aws.s3.BucketObject(
            f"bucket-object-{key}",
            bucket=bucket.id,
            key=key,
            source=pulumi.FileAsset(abs_path),
            content_type=mime
        )

# frontend DNS stuff
us_east_1 = aws.Provider("us-east-1-provider", region="us-east-1")
frontend_cert = aws.acm.Certificate(
    f"{RESOURCE_PREFIX}-frontend-certificate",
    domain_name="boardwise.games",
    subject_alternative_names=["www.boardwise.games"],
    validation_method="DNS",
    opts=pulumi.ResourceOptions(provider=us_east_1)
)

cert_record_base = cloudflare.DnsRecord(
    f"{RESOURCE_PREFIX}-cert-record-base",
    zone_id=settings.CLOUDFLARE_ZONE_ID,
    name=frontend_cert.domain_validation_options[0].resource_record_name,
    type=frontend_cert.domain_validation_options[0].resource_record_type,
    content=frontend_cert.domain_validation_options[0].resource_record_value,
    proxied=False,
    ttl=1
)

cert_record_www = cloudflare.DnsRecord(
    f"{RESOURCE_PREFIX}-cert-record-www",
    zone_id=settings.CLOUDFLARE_ZONE_ID,
    name=frontend_cert.domain_validation_options[1].resource_record_name,
    type=frontend_cert.domain_validation_options[1].resource_record_type,
    content=frontend_cert.domain_validation_options[1].resource_record_value,
    proxied=False,
    ttl=1
)

cert_validation = aws.acm.CertificateValidation(
    f"{RESOURCE_PREFIX}-cert-validation",
    certificate_arn=frontend_cert.arn,
    validation_record_fqdns=[cert_record_base.name, cert_record_www.name],
    opts=pulumi.ResourceOptions(provider=us_east_1)
)

# setting up cloudfront
oac = aws.cloudfront.OriginAccessControl(
    f"{RESOURCE_PREFIX}-cloudfront-oac",
    description="OAC for frontend bucket",
    origin_access_control_origin_type="s3",
    signing_behavior="always",
    signing_protocol="sigv4"
)

frontend_distro = aws.cloudfront.Distribution(
    f"{RESOURCE_PREFIX}-frontend-distro",
    enabled=True,
    is_ipv6_enabled=True,
    default_root_object="index.html",
    aliases=["boardwise.games", "www.boardwise.games"],
    origins=[
        aws.cloudfront.DistributionOriginArgs(
            domain_name=bucket.bucket_regional_domain_name,
            origin_id=bucket.id,
            origin_access_control_id=oac.id
        )
    ],
    default_cache_behavior=aws.cloudfront.DistributionDefaultCacheBehaviorArgs(
        target_origin_id=bucket.id,
        viewer_protocol_policy="redirect-to-https",
        allowed_methods=["GET", "HEAD", "OPTIONS"],
        cached_methods=["GET", "HEAD", "OPTIONS"],
        forwarded_values=aws.cloudfront.DistributionDefaultCacheBehaviorForwardedValuesArgs(
            query_string=False,
            cookies=aws.cloudfront.DistributionDefaultCacheBehaviorForwardedValuesCookiesArgs(
                forward="none"
            )
        ),
        min_ttl=0,
        default_ttl=3600,
        max_ttl=86400
    ),
    restrictions=aws.cloudfront.DistributionRestrictionsArgs(
        geo_restriction=aws.cloudfront.DistributionRestrictionsGeoRestrictionArgs(
            restriction_type="none"
        )
    ),
    viewer_certificate=aws.cloudfront.DistributionViewerCertificateArgs(
        acm_certificate_arn=cert_validation.certificate_arn,
        ssl_support_method="sni-only",
        minimum_protocol_version="TLSv1.2_2021"
    ),
    opts=pulumi.ResourceOptions(depends_on=[bucket])
)

bucket_policy = aws.s3.BucketPolicy(
    f'{RESOURCE_PREFIX}-bucket-policy',
    bucket=bucket.id,
    policy=pulumi.Output.all(
        bucket_arn=bucket.arn,
        frontend_arn=frontend_distro.arn
    ).apply(
        lambda args: json.dumps({
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Action": "s3:GetObject",
                    "Effect": "Allow",
                    "Resource": f"{args['bucket_arn']}/*",
                    "Principal": {"Service": "cloudfront.amazonaws.com"},
                    "Condition": {
                        "StringEquals": {
                            "AWS:SourceArn": args['frontend_arn']
                        }
                    }
                }
            ]
        })
    )
)

base_dns_record = cloudflare.DnsRecord(
    f"{RESOURCE_PREFIX}-base-dns-record",
    zone_id=settings.CLOUDFLARE_ZONE_ID,
    name="boardwise.games",
    type="CNAME",
    content=frontend_distro.domain_name,
    proxied=False,
    ttl=1
)

www_dns_record = cloudflare.DnsRecord(
    f"{RESOURCE_PREFIX}-www-record",
    zone_id=settings.CLOUDFLARE_ZONE_ID,
    name="www.boardwise.games",
    type="CNAME",
    content=frontend_distro.domain_name,
    proxied=False,
    ttl=1
)

pulumi.export("frontend_url", "https://boardwise.games")
pulumi.export("cloudfront_distro_id", frontend_distro.id)