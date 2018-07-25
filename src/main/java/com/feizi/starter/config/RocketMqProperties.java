package com.feizi.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMq配置信息
 * Created by feizi on 2018/6/26.
 */
@ConfigurationProperties(prefix = RocketMqProperties.CONFIG_PREFIX)
public class RocketMqProperties {
    public static final String CONFIG_PREFIX = "spring.rocketmq";

    /**
     * nameServer寻址地址
     */
    private String nameServer;

    /**
     * 生产者
     */
    private RocketMqProducerConfig producerConfig;

    public static class RocketMqProducerConfig {
        /**
         * 生产者组
         */
        private String group;

        /**
         * 发送消息超时时间
         */
        private int sendMsgTimeout = 3000;

        /**
         * 消息压缩阈值：超过4k会被压缩
         */
        private int compressMsgBodyOverHowmuch = 1024 * 4;

        /**
         * 发送同步消息失败后重试次数：2
         */
        private int retryTimesWhenSendFailed = 2;

        /**
         * 发送异步消息失败后重试次数：2
         */
        private int retryTimesWhenSendAsyncFailed = 2;

        /**
         * 发送失败是否立即切换至其他broker
         */
        private boolean retryAnotherBrokerWhenNotStoreOk = false;

        /**
         * 单条消息的最大容量：4M
         */
        private int maxMessageSize = 1024 * 1024 * 4;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public int getSendMsgTimeout() {
            return sendMsgTimeout;
        }

        public void setSendMsgTimeout(int sendMsgTimeout) {
            this.sendMsgTimeout = sendMsgTimeout;
        }

        public int getCompressMsgBodyOverHowmuch() {
            return compressMsgBodyOverHowmuch;
        }

        public void setCompressMsgBodyOverHowmuch(int compressMsgBodyOverHowmuch) {
            this.compressMsgBodyOverHowmuch = compressMsgBodyOverHowmuch;
        }

        public int getRetryTimesWhenSendFailed() {
            return retryTimesWhenSendFailed;
        }

        public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
            this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
        }

        public int getRetryTimesWhenSendAsyncFailed() {
            return retryTimesWhenSendAsyncFailed;
        }

        public void setRetryTimesWhenSendAsyncFailed(int retryTimesWhenSendAsyncFailed) {
            this.retryTimesWhenSendAsyncFailed = retryTimesWhenSendAsyncFailed;
        }

        public boolean isRetryAnotherBrokerWhenNotStoreOk() {
            return retryAnotherBrokerWhenNotStoreOk;
        }

        public void setRetryAnotherBrokerWhenNotStoreOk(boolean retryAnotherBrokerWhenNotStoreOk) {
            this.retryAnotherBrokerWhenNotStoreOk = retryAnotherBrokerWhenNotStoreOk;
        }

        public int getMaxMessageSize() {
            return maxMessageSize;
        }

        public void setMaxMessageSize(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
        }

        @Override
        public String toString() {
            return "RocketMqProducerConfig{" +
                    "group='" + group + '\'' +
                    ", sendMsgTimeout=" + sendMsgTimeout +
                    ", compressMsgBodyOverHowmuch=" + compressMsgBodyOverHowmuch +
                    ", retryTimesWhenSendFailed=" + retryTimesWhenSendFailed +
                    ", retryTimesWhenSendAsyncFailed=" + retryTimesWhenSendAsyncFailed +
                    ", retryAnotherBrokerWhenNotStoreOk=" + retryAnotherBrokerWhenNotStoreOk +
                    ", maxMessageSize=" + maxMessageSize +
                    '}';
        }
    }

    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public RocketMqProducerConfig getProducerConfig() {
        return producerConfig;
    }

    public void setProducerConfig(RocketMqProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }

    @Override
    public String toString() {
        return "RocketMqProperties{" +
                "nameServer='" + nameServer + '\'' +
                ", producerConfig=" + producerConfig +
                '}';
    }
}
