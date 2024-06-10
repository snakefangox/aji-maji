package xyz.fancyteam.ajimaji.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public record ScaledDensityFunction(DensityFunction input, double xScale, double yScale, double zScale)
    implements DensityFunction {
    public static final MapCodec<ScaledDensityFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.CODEC.fieldOf("input").forGetter(ScaledDensityFunction::input),
        Codec.DOUBLE.lenientOptionalFieldOf("x_scale", 1.0).forGetter(ScaledDensityFunction::xScale),
        Codec.DOUBLE.lenientOptionalFieldOf("y_scale", 1.0).forGetter(ScaledDensityFunction::yScale),
        Codec.DOUBLE.lenientOptionalFieldOf("z_scale", 1.0).forGetter(ScaledDensityFunction::zScale)
    ).apply(instance, ScaledDensityFunction::new));

    public static final CodecHolder<ScaledDensityFunction> CODEC_HOLDER = CodecHolder.of(CODEC);

    @Override
    public double sample(NoisePos pos) {
        return input.sample(scale(pos));
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        EachApplier newApplier = new EachApplier() {
            @Override
            public NoisePos at(int index) {
                return scale(applier.at(index));
            }

            @Override
            public void fill(double[] densities, DensityFunction densityFunction) {
                // I *think* this is ok
                applier.fill(densities, new ScaledDensityFunction(densityFunction, xScale, yScale, zScale));
            }
        };

        input.fill(densities, newApplier);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        return visitor.apply(new ScaledDensityFunction(input.apply(visitor), xScale, yScale, zScale));
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return CODEC_HOLDER;
    }

    private NoisePos scale(NoisePos pos) {
        return new NoisePos() {
            @Override
            public int blockX() {
                return (int) Math.round(pos.blockX() * xScale);
            }

            @Override
            public int blockY() {
                return (int) Math.round(pos.blockY() * yScale);
            }

            @Override
            public int blockZ() {
                return (int) Math.round(pos.blockZ() * zScale);
            }
        };
    }
}
