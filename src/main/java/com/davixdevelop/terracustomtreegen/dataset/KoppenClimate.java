package com.davixdevelop.terracustomtreegen.dataset;

import LZMA.LzmaInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.buildtheearth.terraplusplus.dataset.builtin.AbstractBuiltinDataset;
import net.buildtheearth.terraplusplus.util.RLEByteArray;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;

import java.io.InputStream;

import static net.daporkchop.lib.common.math.PMath.floorI;

/**
 * Represents a Koppen-Climate dataset
 * The returned values from 1 to 30 represent the following classes:
 * Af,Am,Aw,BWh,BWk,BSh,BSk,Csa,Csb,Csc,Cwa,Cwb,Cwc,Cfa,Cfb,Cfc,Dsa,Dsb,Dsc,Dsd,Dwa,Dwb,Dwc,Dwd,Dfa,Dfb,Dfc,Dfd,ET,EF
 *
 * @author DavixDevelop
 *
 */
public class KoppenClimate extends AbstractBuiltinDataset {
    protected static  final  int COLUMNS = 43200;
    protected static final int ROWS = 21600;

    public  KoppenClimate(){
        super(COLUMNS, ROWS);
    }

    private static final Ref<RLEByteArray> CACHE = Ref.soft((IOSupplier<RLEByteArray>) () -> {
        ByteBuf buffered;
        try(InputStream is = new LzmaInputStream(KoppenClimate.class.getResourceAsStream("koppen_map.lzma"))){
            buffered = Unpooled.wrappedBuffer(StreamUtil.toByteArray(is));
        }

        RLEByteArray.Builder builder = RLEByteArray.builder();
        for(int i = 0, s = buffered.readableBytes(); i < s; i++){
            builder.append(buffered.getByte(i));
        }

        return builder.build();
    });

    private  final  RLEByteArray data = CACHE.get();

    @Override
    protected double get(double xc, double yc) {
        int x = floorI(xc);
        int y = floorI(yc);

        if(x >= COLUMNS || x < 0 || y >= ROWS || y < 0)
            return 0;

        return  this.data.get(y * COLUMNS + x);
    }
}
