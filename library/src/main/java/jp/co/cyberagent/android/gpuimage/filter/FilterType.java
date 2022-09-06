package jp.co.cyberagent.android.gpuimage.filter;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * ================================================
 * Created by JessYan on 2020/6/2 16:17
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public enum FilterType {
    DEFAULT("Normal"),
    CLARENDON("CLARENDON"),
    I_HUDSON("Hudson"),
    I_INKWELL("Inkwell"),
    JUNO("JUNO"),
    I_EARLYBIRD("Earlybird"),
    I_HEFE("Hefe"),
    I_LOMO("Lomo"),
    I_NASHVILLE("Nashville"),
    I_LORDKELVIN("LordKelvin"),
    I_RISE("Rise"),
    I_SIERRA("Sierra"),
    I_SUTRO("Sutro"),
    I_TOASTER("Toaster"),
    I_VALENCIA("Valencia"),
    I_WALDEN("Walden"),
    X_PRO_II("X-Pro II");

    private String name;

    FilterType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<FilterType> createFilterList() {
        return Arrays.asList(FilterType.values());
    }

    public static List<GPUImageFilter> createImageFilterList(Context context) {
        List<GPUImageFilter> imageFilters = new ArrayList<>();
        for (FilterType filterType : FilterType.values()) {
            imageFilters.add(FilterType.createFilterForType(context, filterType));
        }
        return imageFilters;
    }

    public static GPUImageFilter createFilterForType(final Context context, final FilterType type) {
        switch (type){
            case DEFAULT:
                return new GPUImageFilter();
            case CLARENDON:
                return new IF1977Filter(context);
            case JUNO:
                return new IFAmaroFilter(context);
//            case I_BRANNAN:
//                return new IFBrannanFilter(context);
            case I_EARLYBIRD:
                return new IFEarlybirdFilter(context);
            case I_HEFE:
                return new IFHefeFilter(context);
            case I_HUDSON:
                return new IFHudsonFilter(context);
            case I_INKWELL:
                return new IFInkwellFilter(context);
            case I_LOMO:
                return new IFLomoFilter(context);
           
            case I_LORDKELVIN:
                return new IFLordKelvinFilter(context);
            case I_NASHVILLE:
                return new IFNashvilleFilter(context);
            case I_RISE:
                return new IFRiseFilter(context);
            case I_SIERRA:
                return new IFSierraFilter(context);
            case I_SUTRO:
                return new IFSutroFilter(context);
            case I_TOASTER:
                return new IFToasterFilter(context);
            case I_VALENCIA:
                return new IFValenciaFilter(context);
            case I_WALDEN:
                return new IFWaldenFilter(context);
            case X_PRO_II:
                return new IFXprollFilter(context);
            default:
                throw new IllegalStateException("No filter of that type!");
        }
    }
}
