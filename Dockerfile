FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.1

# nori 분석기 설치
RUN elasticsearch-plugin install --batch analysis-nori

# 데이터 권한 문제 방지
RUN chmod -R 777 /usr/share/elasticsearch/data