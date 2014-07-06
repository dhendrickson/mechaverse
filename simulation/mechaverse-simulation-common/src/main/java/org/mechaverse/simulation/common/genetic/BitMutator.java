package org.mechaverse.simulation.common.genetic;

import gnu.trove.list.array.TIntArrayList;

import java.util.Random;

import org.uncommons.maths.random.BinomialGenerator;

import com.google.common.base.Preconditions;

/**
 * Mutates bits in a byte array at random with a given probability.
 *
 * @author thorntonv@mechaverse.org
 */
public class BitMutator implements Mutator {

  private static final int BITS_PER_BYTE = 8;

  private final float bitMutationProbability;
  private final Random random;

  public BitMutator(float bitMutationProbability, Random random) {
    Preconditions.checkArgument(bitMutationProbability >= 0.0f && bitMutationProbability <= 1.0f);
    this.bitMutationProbability = bitMutationProbability;
    this.random = Preconditions.checkNotNull(random);
  }

  @Override
  public void mutate(byte[] data) {
    Preconditions.checkNotNull(data);

    int bitCount = data.length * BITS_PER_BYTE;
    int numBitsToMutate = getNumBitsToMutate(bitCount);

    TIntArrayList mutatedBits = new TIntArrayList(numBitsToMutate);
    for (int cnt = 1; cnt <= numBitsToMutate; cnt++) {
      // Mutate one of the remaining bits at random.
      int bitToMutateIdx = random.nextInt(bitCount);
      while (mutatedBits.contains(bitToMutateIdx)) {
        bitToMutateIdx = random.nextInt(bitCount);
      }
      mutateBit(data, bitToMutateIdx);
      mutatedBits.add(bitToMutateIdx);
    }
  }

  public static void mutateBit(byte[] data, int bitIndex) {
    int arrayIndex = bitIndex / BITS_PER_BYTE;
    int bitInByte = bitIndex % BITS_PER_BYTE;
    data[arrayIndex] ^= (1 << bitInByte);
  }

  private int getNumBitsToMutate(int bitCount) {
    if (bitMutationProbability == 0) {
      return 0;
    } else if (bitMutationProbability == 1) {
      return bitCount;
    }

    return new BinomialGenerator(bitCount, bitMutationProbability, random).nextValue();
  }
}
