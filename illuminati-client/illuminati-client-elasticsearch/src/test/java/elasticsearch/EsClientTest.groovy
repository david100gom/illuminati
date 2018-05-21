package elasticsearch

import com.leekyoungil.illuminati.common.http.IlluminatiHttpClient
import com.leekyoungil.illuminati.elasticsearch.infra.ESclientImpl
import com.leekyoungil.illuminati.elasticsearch.infra.EsClient
import com.leekyoungil.illuminati.elasticsearch.infra.model.EsData
import com.leekyoungil.illuminati.elasticsearch.infra.model.EsDataImpl
import spock.lang.Specification

import java.text.SimpleDateFormat

class EsClientTest extends Specification {

    private final String elasticSearchHost = "pi.leekyoungil.com";
    private final int elasticSearchPort = 9200;

    def "get all value in a field from elasticsearch" () {
        setup:
        List<String> fields = new ArrayList<>();
        fields.add("jvmInfo");
        fields.add("timestamp");
        Map<String, Object> param =  new HashMap<>();
        param.put("source", fields)

        EsClient esClient = new ESclientImpl(new IlluminatiHttpClient(), this.elasticSearchHost, this.elasticSearchPort);
        esClient.setOptionalIndex("sample-illuminati*");

        when:
        String data = esClient.getDataByParam(param);

        then:
        data != null;
    }

    def "make jvm data from source data" () {
        setup:
        List<String> fields = new ArrayList<>();
        fields.add("jvmInfo");
        fields.add("timestamp");
        Map<String, Object> param =  new HashMap<>();
        param.put("source", fields)

        EsClient esClient = new ESclientImpl(new IlluminatiHttpClient(), this.elasticSearchHost, this.elasticSearchPort);
        esClient.setOptionalIndex("sample-illuminati*");
        String data = esClient.getDataByParam(param);

        when:
        EsData esData = new EsDataImpl(data);
        List<Map<String, Object>> resultList = esData.getEsDataList();

        then:
        resultList.size() > 0
        resultList.each { map ->
            map.containsKey("source") == true;
            Map<String, Object> checkMap = map.get("source");
            checkMap.containsKey("jvmInfo") == true;
        }
    }

    def "jvm data by range" () {
        setup:
        List<String> fields = new ArrayList<>();
        fields.add("jvmInfo");
        fields.add("timestamp");

        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date from = transFormat.parse("2018-03-05 00:04:20");
        Date to = transFormat.parse("2018-03-05 00:6:20");

        Map<String, Object> rangeTimestamp = new HashMap<>();
        rangeTimestamp.put("gte", "2018-03-04T00:04:20");
        rangeTimestamp.put("lte", "2018-03-05T00:06:20");

        Map<String, Object> range = new HashMap<>();
        range.put("logTime", rangeTimestamp);

        Map<String, Object> sort = new HashMap<>();
        sort.put("logTime", "desc");

        Map<String, Object> param =  new HashMap<>();
        param.put("source", fields);
        param.put("range", range);
        param.put("sort", sort);
        param.put("size", 20)
        param.put("from", 10);

        EsClient esClient = new ESclientImpl(new IlluminatiHttpClient(), this.elasticSearchHost, this.elasticSearchPort);
        esClient.setOptionalIndex("sample-illuminati*");
        String data = esClient.getDataByParam(param);

        when:
        EsData esData = new EsDataImpl(data);
        List<Map<String, Object>> resultList = esData.getEsDataList();

        then:
        resultList.size() > 0
        resultList.each { map ->
            map.containsKey("source") == true;
            Map<String, Object> checkMap = map.get("source");
            checkMap.containsKey("jvmInfo") == true;
        }
    }
}