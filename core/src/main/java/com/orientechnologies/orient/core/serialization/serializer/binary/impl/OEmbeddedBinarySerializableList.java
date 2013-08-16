package com.orientechnologies.orient.core.serialization.serializer.binary.impl;

import java.util.AbstractList;

import com.orientechnologies.common.serialization.types.OBinarySerializer;
import com.orientechnologies.common.serialization.types.OByteSerializer;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;

/**
 * @author Andrey Lomakin
 * @since 8/13/13
 */
public class OEmbeddedBinarySerializableList<T> extends AbstractList<T> {
  private static final int     SERIALIZER_ID_OFFSET = 0;
  private static final int     SIZE_OFFSET          = SERIALIZER_ID_OFFSET + OByteSerializer.BYTE_SIZE;
  private static final int     FREE_POINTER_OFFSET  = SIZE_OFFSET + OIntegerSerializer.INT_SIZE;
  private static final int     ITEMS_OFFSET         = FREE_POINTER_OFFSET + OIntegerSerializer.INT_SIZE;

  private static final int     INITIAL_SIZE         = 1024;
  private OBinarySerializer<T> itemSerializer;

  byte[]                       content              = new byte[INITIAL_SIZE];

  public OEmbeddedBinarySerializableList() {
  }

  @Override
  public int size() {
    return OIntegerSerializer.INSTANCE.deserialize(content, SIZE_OFFSET);
  }

  @Override
  public T get(int index) {
    if (index >= size() || index < 0)
      throw new IndexOutOfBoundsException();

    final int itemPosition = OIntegerSerializer.INSTANCE.deserialize(content, ITEMS_OFFSET + OIntegerSerializer.INT_SIZE * index);
    return itemSerializer.deserialize(content, itemPosition);
  }

  @Override
  public T set(int index, T element) {
    if (index >= size() || index < 0)
      throw new IndexOutOfBoundsException();

    final int itemPosition = OIntegerSerializer.INSTANCE.deserialize(content, ITEMS_OFFSET + OIntegerSerializer.INT_SIZE * index);
    final T previousItem = itemSerializer.deserialize(content, itemPosition);

    if (itemSerializer.isFixedLength()) {
      itemSerializer.serialize(element, content, itemPosition);
    } else {
      remove(index);
      add(index, element);
    }

    return previousItem;
  }

  @Override
  public void add(int index, T element) {
    int size = size();
    if (index > size || index < 0)
      throw new IndexOutOfBoundsException();

    if (itemSerializer == null) {
      itemSerializer = (OBinarySerializer<T>) OBinarySerializerFactory.INSTANCE.getObjectSerializer(OType.getTypeByClass(element
          .getClass()));
      content[SERIALIZER_ID_OFFSET] = itemSerializer.getId();
    }

    int freePosition = OIntegerSerializer.INSTANCE.deserialize(content, FREE_POINTER_OFFSET);
    final int itemSize = itemSerializer.getObjectSize(element);

    if (freePosition - itemSize < ITEMS_OFFSET + (size + 1) * OIntegerSerializer.INT_SIZE) {
      freePosition = explode();
    }
    freePosition -= itemSize;

    final int indexPositionToInsert = index * OIntegerSerializer.INT_SIZE;
    if (index < size) {
      System.arraycopy(content, indexPositionToInsert, content, indexPositionToInsert + OIntegerSerializer.INT_SIZE, (size - index)
          * OIntegerSerializer.INT_SIZE);
    }

    itemSerializer.serialize(element, content, freePosition);

    OIntegerSerializer.INSTANCE.serialize(freePosition, content, indexPositionToInsert);
    OIntegerSerializer.INSTANCE.serialize(size + 1, content, SIZE_OFFSET);
  }

  private int explode() {
    return -1;
  }
}
