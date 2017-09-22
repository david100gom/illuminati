package com.leekyoungil.illuminati.esconsumer.listener;

import com.leekyoungil.illuminati.client.prossor.init.IlluminatiClientInit;
import com.leekyoungil.illuminati.common.constant.IlluminatiConstant;
import com.leekyoungil.illuminati.elasticsearch.infra.EsClient;
import com.leekyoungil.illuminati.esconsumer.config.model.SampleEsModelImpl;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@EnableBinding(Sink.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SubscribeListener {

    private static final Logger SUB_LOGGER = LoggerFactory.getLogger(SubscribeListener.class);

    private final @NotNull EsClient esClient;

    @StreamListener(Sink.INPUT)
    public void subscribe (Message<?> message) {
        SampleEsModelImpl sampleBuyEsModel = null;

        try {
            String jsonString = (String) message.getPayload();
            sampleBuyEsModel = IlluminatiConstant.ILLUMINATI_GSON_OBJ.fromJson(jsonString, SampleEsModelImpl.class);
        } catch (Exception ex) {
            SUB_LOGGER.error("Sorry. something is wrong in Parsing received dto. ("+ex.toString()+")");
            return;
        }

        if (sampleBuyEsModel != null) {
            // if your Application (an application that generates illuminati dto) is using grails. have to this code activation.
            //String[] excludeMethodName = new String[]{".getMetaClass()", ".getProperty(java.lang.String)"};
            //for (String excludeMethodNameData : excludeMethodName) {
            //    if (sampleBuyEsModel.getMethodName().indexOf(excludeMethodNameData) > -1) {
            //        return;
            //    }
            //}

            sampleBuyEsModel.customData();
            HttpResponse result = (HttpResponse) this.esClient.save(sampleBuyEsModel);

            if ("2".equals(String.valueOf(result.getStatusLine().getStatusCode()).substring(0, 1))) {
                SUB_LOGGER.info("successfully transferred dto to Elasticsearch.");
            } else {
                SUB_LOGGER.error("Sorry. something is wrong in send to Elasticsearch. (code : "+result.getStatusLine().getStatusCode()+")");
            }
        }
    }
}
