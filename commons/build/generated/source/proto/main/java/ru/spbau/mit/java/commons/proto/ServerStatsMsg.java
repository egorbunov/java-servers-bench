// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ru/spbau/mit/java/message.proto

package ru.spbau.mit.java.commons.proto;

/**
 * Protobuf type {@code ServerStatsMsg}
 */
public  final class ServerStatsMsg extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ServerStatsMsg)
    ServerStatsMsgOrBuilder {
  // Use ServerStatsMsg.newBuilder() to construct.
  private ServerStatsMsg(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ServerStatsMsg() {
    avRequestMs_ = 0D;
    avSortingMs_ = 0D;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private ServerStatsMsg(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 9: {

            avRequestMs_ = input.readDouble();
            break;
          }
          case 17: {

            avSortingMs_ = input.readDouble();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return ru.spbau.mit.java.commons.proto.Protos.internal_static_ServerStatsMsg_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return ru.spbau.mit.java.commons.proto.Protos.internal_static_ServerStatsMsg_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            ru.spbau.mit.java.commons.proto.ServerStatsMsg.class, ru.spbau.mit.java.commons.proto.ServerStatsMsg.Builder.class);
  }

  public static final int AVREQUESTMS_FIELD_NUMBER = 1;
  private double avRequestMs_;
  /**
   * <code>optional double avRequestMs = 1;</code>
   */
  public double getAvRequestMs() {
    return avRequestMs_;
  }

  public static final int AVSORTINGMS_FIELD_NUMBER = 2;
  private double avSortingMs_;
  /**
   * <code>optional double avSortingMs = 2;</code>
   */
  public double getAvSortingMs() {
    return avSortingMs_;
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (avRequestMs_ != 0D) {
      output.writeDouble(1, avRequestMs_);
    }
    if (avSortingMs_ != 0D) {
      output.writeDouble(2, avSortingMs_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (avRequestMs_ != 0D) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(1, avRequestMs_);
    }
    if (avSortingMs_ != 0D) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(2, avSortingMs_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof ru.spbau.mit.java.commons.proto.ServerStatsMsg)) {
      return super.equals(obj);
    }
    ru.spbau.mit.java.commons.proto.ServerStatsMsg other = (ru.spbau.mit.java.commons.proto.ServerStatsMsg) obj;

    boolean result = true;
    result = result && (
        java.lang.Double.doubleToLongBits(getAvRequestMs())
        == java.lang.Double.doubleToLongBits(
            other.getAvRequestMs()));
    result = result && (
        java.lang.Double.doubleToLongBits(getAvSortingMs())
        == java.lang.Double.doubleToLongBits(
            other.getAvSortingMs()));
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (37 * hash) + AVREQUESTMS_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getAvRequestMs()));
    hash = (37 * hash) + AVSORTINGMS_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getAvSortingMs()));
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(ru.spbau.mit.java.commons.proto.ServerStatsMsg prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code ServerStatsMsg}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ServerStatsMsg)
      ru.spbau.mit.java.commons.proto.ServerStatsMsgOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ru.spbau.mit.java.commons.proto.Protos.internal_static_ServerStatsMsg_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ru.spbau.mit.java.commons.proto.Protos.internal_static_ServerStatsMsg_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ru.spbau.mit.java.commons.proto.ServerStatsMsg.class, ru.spbau.mit.java.commons.proto.ServerStatsMsg.Builder.class);
    }

    // Construct using ru.spbau.mit.java.commons.proto.ServerStatsMsg.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      avRequestMs_ = 0D;

      avSortingMs_ = 0D;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return ru.spbau.mit.java.commons.proto.Protos.internal_static_ServerStatsMsg_descriptor;
    }

    public ru.spbau.mit.java.commons.proto.ServerStatsMsg getDefaultInstanceForType() {
      return ru.spbau.mit.java.commons.proto.ServerStatsMsg.getDefaultInstance();
    }

    public ru.spbau.mit.java.commons.proto.ServerStatsMsg build() {
      ru.spbau.mit.java.commons.proto.ServerStatsMsg result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public ru.spbau.mit.java.commons.proto.ServerStatsMsg buildPartial() {
      ru.spbau.mit.java.commons.proto.ServerStatsMsg result = new ru.spbau.mit.java.commons.proto.ServerStatsMsg(this);
      result.avRequestMs_ = avRequestMs_;
      result.avSortingMs_ = avSortingMs_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof ru.spbau.mit.java.commons.proto.ServerStatsMsg) {
        return mergeFrom((ru.spbau.mit.java.commons.proto.ServerStatsMsg)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(ru.spbau.mit.java.commons.proto.ServerStatsMsg other) {
      if (other == ru.spbau.mit.java.commons.proto.ServerStatsMsg.getDefaultInstance()) return this;
      if (other.getAvRequestMs() != 0D) {
        setAvRequestMs(other.getAvRequestMs());
      }
      if (other.getAvSortingMs() != 0D) {
        setAvSortingMs(other.getAvSortingMs());
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      ru.spbau.mit.java.commons.proto.ServerStatsMsg parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (ru.spbau.mit.java.commons.proto.ServerStatsMsg) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private double avRequestMs_ ;
    /**
     * <code>optional double avRequestMs = 1;</code>
     */
    public double getAvRequestMs() {
      return avRequestMs_;
    }
    /**
     * <code>optional double avRequestMs = 1;</code>
     */
    public Builder setAvRequestMs(double value) {
      
      avRequestMs_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional double avRequestMs = 1;</code>
     */
    public Builder clearAvRequestMs() {
      
      avRequestMs_ = 0D;
      onChanged();
      return this;
    }

    private double avSortingMs_ ;
    /**
     * <code>optional double avSortingMs = 2;</code>
     */
    public double getAvSortingMs() {
      return avSortingMs_;
    }
    /**
     * <code>optional double avSortingMs = 2;</code>
     */
    public Builder setAvSortingMs(double value) {
      
      avSortingMs_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional double avSortingMs = 2;</code>
     */
    public Builder clearAvSortingMs() {
      
      avSortingMs_ = 0D;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:ServerStatsMsg)
  }

  // @@protoc_insertion_point(class_scope:ServerStatsMsg)
  private static final ru.spbau.mit.java.commons.proto.ServerStatsMsg DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new ru.spbau.mit.java.commons.proto.ServerStatsMsg();
  }

  public static ru.spbau.mit.java.commons.proto.ServerStatsMsg getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ServerStatsMsg>
      PARSER = new com.google.protobuf.AbstractParser<ServerStatsMsg>() {
    public ServerStatsMsg parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new ServerStatsMsg(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ServerStatsMsg> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ServerStatsMsg> getParserForType() {
    return PARSER;
  }

  public ru.spbau.mit.java.commons.proto.ServerStatsMsg getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

