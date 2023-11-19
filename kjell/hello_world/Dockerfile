FROM python:3.9
# Or any preferred Python version.
ADD . .

ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_ACCESS_KEY=""
ENV AWS_REGION="eu-west-1"
ENV BUCKET_NAME="kandidat-id-2012"
RUN pip3 install -r requirements.txt
RUN pip install awscli
CMD ["python3", "./app.py"]
# Or enter the name of your unique directory and parameter set.