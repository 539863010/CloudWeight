package xm.cloudweight.utils.bussiness;

import android.util.Log;

import com.xmzynt.storm.service.sort.SortOutData;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.Comparator;

/**
 * @author wyh
 * @description: 分拣过滤器
 * @create 2017/11/15
 */
public class ListComparator implements Comparator<SortOutData> {

    private double mDobWeight = 0;

    public void setDobWeight(double dobWeight) {
        mDobWeight = dobWeight;
    }

    public ListComparator(double weight) {
        mDobWeight = weight;
    }

    @Override
    public int compare(SortOutData s1, SortOutData s2) {
//        double absS1 = Math.abs(s1.getCoverToKgQty().doubleValue() - mDobWeight);
//        double absS2 = Math.abs(s2.getCoverToKgQty().doubleValue() - mDobWeight);
//        if (absS1 > absS2) {
//            return 1;
//        } else {
//            return -1;
//        }
        double absS1 = Math.abs(s1.getCoverToKgQty().doubleValue() - mDobWeight);
        double absS2 = Math.abs(s2.getCoverToKgQty().doubleValue() - mDobWeight);
        if (absS1 > absS2) {
            return 1;
        } else if (absS1 == absS2) {
            String customer1 = getPingYin(s1.getCustomer().getName());
            String customer2 = getPingYin(s2.getCustomer().getName());
            return customer1.compareTo(customer2);
        } else {
            return -1;
        }
    }

    /**
     * 将字符串中的中文转化为拼音,其他字符不变
     *
     * @param inputString
     * @return
     */
    private String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();// 把字符串转化成字符数组
        String output = "";

        try {
            for (int i = 0; i < input.length; i++) {
                // \\u4E00是unicode编码，判断是不是中文
                if (java.lang.Character.toString(input[i]).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    // 将汉语拼音的全拼存到temp数组
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                            input[i], format);
                    // 取拼音的第一个读音
                    output += temp[0];
                }
                // 大写字母转化成小写字母
                else if (input[i] > 'A' && input[i] < 'Z') {
                    output += java.lang.Character.toString(input[i]);
                    output = output.toLowerCase();
                }
                output += java.lang.Character.toString(input[i]);
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return output;
    }
}