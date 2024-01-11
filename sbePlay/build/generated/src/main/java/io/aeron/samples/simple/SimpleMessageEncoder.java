/* Generated SBE (Simple Binary Encoding) message codec. */
package io.aeron.samples.simple;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;


/**
 * A simple example message
 */
@SuppressWarnings("all")
public final class SimpleMessageEncoder
{
    public static final int BLOCK_LENGTH = 44;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 101;
    public static final int SCHEMA_VERSION = 1;
    public static final String SEMANTIC_VERSION = "1";
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final SimpleMessageEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    private int initialOffset;
    private int offset;
    private int limit;

    public int sbeBlockLength()
    {
        return BLOCK_LENGTH;
    }

    public int sbeTemplateId()
    {
        return TEMPLATE_ID;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String sbeSemanticType()
    {
        return "";
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int initialOffset()
    {
        return initialOffset;
    }

    public int offset()
    {
        return offset;
    }

    public SimpleMessageEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.initialOffset = offset;
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public SimpleMessageEncoder wrapAndApplyHeader(
        final MutableDirectBuffer buffer, final int offset, final MessageHeaderEncoder headerEncoder)
    {
        headerEncoder
            .wrap(buffer, offset)
            .blockLength(BLOCK_LENGTH)
            .templateId(TEMPLATE_ID)
            .schemaId(SCHEMA_ID)
            .version(SCHEMA_VERSION);

        return wrap(buffer, offset + MessageHeaderEncoder.ENCODED_LENGTH);
    }

    public int encodedLength()
    {
        return limit - offset;
    }

    public int limit()
    {
        return limit;
    }

    public void limit(final int limit)
    {
        this.limit = limit;
    }

    public static int messageIdId()
    {
        return 1;
    }

    public static int messageIdSinceVersion()
    {
        return 0;
    }

    public static int messageIdEncodingOffset()
    {
        return 0;
    }

    public static int messageIdEncodingLength()
    {
        return 8;
    }

    public static String messageIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static long messageIdNullValue()
    {
        return -9223372036854775808L;
    }

    public static long messageIdMinValue()
    {
        return -9223372036854775807L;
    }

    public static long messageIdMaxValue()
    {
        return 9223372036854775807L;
    }

    public SimpleMessageEncoder messageId(final long value)
    {
        buffer.putLong(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int correlationIdId()
    {
        return 2;
    }

    public static int correlationIdSinceVersion()
    {
        return 0;
    }

    public static int correlationIdEncodingOffset()
    {
        return 8;
    }

    public static int correlationIdEncodingLength()
    {
        return 36;
    }

    public static String correlationIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static byte correlationIdNullValue()
    {
        return (byte)0;
    }

    public static byte correlationIdMinValue()
    {
        return (byte)32;
    }

    public static byte correlationIdMaxValue()
    {
        return (byte)126;
    }

    public static int correlationIdLength()
    {
        return 36;
    }


    public SimpleMessageEncoder correlationId(final int index, final byte value)
    {
        if (index < 0 || index >= 36)
        {
            throw new IndexOutOfBoundsException("index out of range: index=" + index);
        }

        final int pos = offset + 8 + (index * 1);
        buffer.putByte(pos, value);

        return this;
    }

    public static String correlationIdCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.US_ASCII.name();
    }

    public SimpleMessageEncoder putCorrelationId(final byte[] src, final int srcOffset)
    {
        final int length = 36;
        if (srcOffset < 0 || srcOffset > (src.length - length))
        {
            throw new IndexOutOfBoundsException("Copy will go out of range: offset=" + srcOffset);
        }

        buffer.putBytes(offset + 8, src, srcOffset, length);

        return this;
    }

    public SimpleMessageEncoder correlationId(final String src)
    {
        final int length = 36;
        final int srcLength = null == src ? 0 : src.length();
        if (srcLength > length)
        {
            throw new IndexOutOfBoundsException("String too large for copy: byte length=" + srcLength);
        }

        buffer.putStringWithoutLengthAscii(offset + 8, src);

        for (int start = srcLength; start < length; ++start)
        {
            buffer.putByte(offset + 8 + start, (byte)0);
        }

        return this;
    }

    public SimpleMessageEncoder correlationId(final CharSequence src)
    {
        final int length = 36;
        final int srcLength = null == src ? 0 : src.length();
        if (srcLength > length)
        {
            throw new IndexOutOfBoundsException("CharSequence too large for copy: byte length=" + srcLength);
        }

        buffer.putStringWithoutLengthAscii(offset + 8, src);

        for (int start = srcLength; start < length; ++start)
        {
            buffer.putByte(offset + 8 + start, (byte)0);
        }

        return this;
    }

    public static int contentId()
    {
        return 3;
    }

    public static String contentCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.UTF_8.name();
    }

    public static String contentMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int contentHeaderLength()
    {
        return 4;
    }

    public SimpleMessageEncoder putContent(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public SimpleMessageEncoder putContent(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public SimpleMessageEncoder content(final String value)
    {
        final byte[] bytes = (null == value || value.isEmpty()) ? org.agrona.collections.ArrayUtil.EMPTY_BYTE_ARRAY : value.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        final int length = bytes.length;
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, bytes, 0, length);

        return this;
    }

    public String toString()
    {
        if (null == buffer)
        {
            return "";
        }

        return appendTo(new StringBuilder()).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        if (null == buffer)
        {
            return builder;
        }

        final SimpleMessageDecoder decoder = new SimpleMessageDecoder();
        decoder.wrap(buffer, initialOffset, BLOCK_LENGTH, SCHEMA_VERSION);

        return decoder.appendTo(builder);
    }
}
