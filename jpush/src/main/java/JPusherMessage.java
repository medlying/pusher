import cn.jiguang.common.DeviceType;
import cn.jiguang.common.utils.Preconditions;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.SMS;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.audience.AudienceType;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.sodacar.guice.modules.env.Env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bosong
 * @date 2019-09-11
 * @desc 极光推送发送消息体
 */

public class JPusherMessage implements PusherMessage {

    //是否推送生产坏境
    public static final Boolean apnsProduction = apnsProduction();

    public static Boolean apnsProduction() {
        return Boolean.valueOf(Env.group("jpush").prop("apns_production").orElse("false"));
    }

    private final String title;
    private final String content;
    private final String contentType;
    private final Map<String, String> extras;
    private final Platform.Builder platformBuilder;
    private final Builder.Type type;
    private final Audience.Builder audienceBuilder;
    private final Notification.Builder notificationBuilder;
    private final Message.Builder messageBuilder;
    private final Options.Builder optionsBuilder;
    private final SMS.Builder smsBuilder;
    private String cid;

    public JPusherMessage(Builder.Type type, String title, String content,
                          String contentType, Map<String, String> extrasBuilder,
                          Platform.Builder platformBuilder, Audience.Builder audienceBuilder,
                          Notification.Builder notificationBuilder, Message.Builder messageBuilder,
                          Options.Builder optionsBuilder, SMS.Builder smsBuilder, String cid) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.contentType = contentType;
        this.extras = extrasBuilder;
        this.platformBuilder = platformBuilder;
        this.audienceBuilder = audienceBuilder;
        this.notificationBuilder = notificationBuilder;
        this.messageBuilder = messageBuilder;
        this.optionsBuilder = optionsBuilder;
        this.smsBuilder = smsBuilder;
        this.cid = cid;
    }

    public Builder builder(Builder.Type type) {
        return new Builder(type);
    }

    @Override
    public <T> T build() {
        optionsBuilder.setApnsProduction(apnsProduction);
        final PushPayload.Builder builder = PushPayload.newBuilder()
                .setPlatform(platformBuilder.build())
                .setAudience(audienceBuilder.build())
                .setSMS(smsBuilder.build())
                .setOptions(optionsBuilder.build())
                .setCid(cid);
        switch (type) {
            case NOTIFICATION:
                builder.setNotification(notificationBuilder
                        .setAlert(content)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(content)
                                .setTitle(title)
                                .addExtras(extras)
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(content)
                                .build())
                        .build());
                break;
            case MESSAGE:
                builder.setMessage(messageBuilder
                        .setContentType(contentType)
                        .setMsgContent(content)
                        .setTitle(title)
                        .addExtras(extras)
                        .build());
                break;
            default:
                break;
        }
        return (T) builder.build();
    }



    private static class Builder {
        private Platform.Builder platformBuilder = null;
        private Audience.Builder audienceBuilder = null;
        private Notification.Builder notificationBuilder = null;
        private AndroidNotification.Builder androidNotificationBuilder = null;
        private IosNotification.Builder iosNotificationBuilder = null;
        private Message.Builder messageBuilder = null;
        private Options.Builder optionsBuilder = null;
        private SMS.Builder smsBuilder = null;
        private String cid;
        private Type type;

        private String title;
        private String content;
        private String contentType;
        private Map<String, String> extrasBuilder;

        enum Type {
            /**
             * notification
             */
            NOTIFICATION("通知"),
            /**
             * message
             */
            MESSAGE("应用内消息");

            Type(final String value) {
                this.value = value;
            }
            private final String value;

            public String getValue() {
                return value;
            }
        }

        Builder(Type type) {
            this.type = type;
            switch (type) {
                case NOTIFICATION:
                    notificationBuilder = Notification.newBuilder();
                    break;
                case MESSAGE:
                    messageBuilder = Message.newBuilder();
                    break;
                default:
                    break;
            }
        }

        public Builder title(String title) {
            this.title =  title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder platform(List<DeviceType> deviceTypes) {
            if (null == platformBuilder) {
                platformBuilder = Platform.newBuilder();
            }
            deviceTypes.forEach(platformBuilder::addDeviceType);
            return this;
        }

        public Builder platform(Boolean all) {
            if (null == platformBuilder) {
                platformBuilder = Platform.newBuilder();
            }
            platformBuilder.setAll(all);
            return this;
        }

        public Builder audience(AudienceType audienceType, List<String> audiences) {
            if (null == audienceBuilder) {
                audienceBuilder = Audience.newBuilder();
            }
            switch (audienceType) {
                case ALIAS:
                    audienceBuilder.addAudienceTarget(AudienceTarget.alias(audiences));
                    break;
                case TAG:
                    audienceBuilder.addAudienceTarget(AudienceTarget.tag(audiences));
                    break;
                case REGISTRATION_ID:
                    audienceBuilder.addAudienceTarget(AudienceTarget.registrationId(audiences));
                    break;
                case TAG_AND:
                    audienceBuilder.addAudienceTarget(AudienceTarget.tag_and(audiences));
                    break;
                case TAG_NOT:
                    audienceBuilder.addAudienceTarget(AudienceTarget.tag_not(audiences));
                    break;
                case SEGMENT:
                    audienceBuilder.addAudienceTarget(AudienceTarget.segment(audiences));
                    break;
                case ABTEST:
                    audienceBuilder.addAudienceTarget(AudienceTarget.abTest(audiences));
                    break;
                default:
                    break;
            }
            return this;
        }

        public Builder audience(Boolean all) {
            if (null == audienceBuilder) {
                audienceBuilder = Audience.newBuilder();
            }
            audienceBuilder.setAll(all);
            return this;
        }

        public Builder options(long timeToLive) {
            if (null == optionsBuilder) {
                optionsBuilder = Options.newBuilder();
            }
            optionsBuilder.setTimeToLive(timeToLive);
            return this;
        }

        public Builder options(int sendNo) {
            if (null == optionsBuilder) {
                optionsBuilder = Options.newBuilder();
            }
            optionsBuilder.setSendno(sendNo);
            return this;
        }

        public Builder cid(String cid) {
            this.cid = cid;
            return this;
        }

        public Builder androidNotificationBuilder(AndroidNotification.Builder androidNotificationBuilder) {
            this.androidNotificationBuilder = androidNotificationBuilder;
            return this;
        }

        public Builder iosNotificationBuilder(IosNotification.Builder iosNotificationBuilder) {
            this.iosNotificationBuilder = this.iosNotificationBuilder;
            return this;
        }

        public Builder extra(String key, String value) {
            Preconditions.checkArgument(! (null == key || null == value), "Key/Value should not be null.");
            if (null == extrasBuilder) {
                extrasBuilder = new HashMap<>();
            }
            extrasBuilder.put(key, value);
            return this;
        }

        public Builder extra(Map<String, String> extras) {
            Preconditions.checkArgument(! (null == extras), "extras should not be null.");
            if (null == extrasBuilder) {
                extrasBuilder = new HashMap<>();
            }
            extrasBuilder.putAll(extras);
            return this;
        }

        public JPusherMessage build() {
            Preconditions.checkArgument(! (content == null), "content must not be null");
            Preconditions.checkArgument(! (Type.NOTIFICATION == null && null == iosNotificationBuilder && null == androidNotificationBuilder), "" +
                    "notification must have ios notification or android");
            return new JPusherMessage(type, title, content, contentType,
                    extrasBuilder, platformBuilder, audienceBuilder,
                    notificationBuilder, messageBuilder, optionsBuilder,
                    smsBuilder, cid);
        }
    }
}
