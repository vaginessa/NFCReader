/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.ansen.card;

import android.content.IntentFilter;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.util.Log;

import com.ansen.Util;
import com.ansen.card.pboc.PbocCard;

import java.io.IOException;
import java.nio.charset.Charset;

import static android.nfc.tech.MifareClassic.SIZE_1K;
import static android.nfc.tech.MifareClassic.SIZE_2K;
import static android.nfc.tech.MifareClassic.SIZE_4K;
import static android.nfc.tech.MifareClassic.SIZE_MINI;
import static android.nfc.tech.MifareClassic.TYPE_CLASSIC;
import static android.nfc.tech.MifareClassic.TYPE_PLUS;
import static android.nfc.tech.MifareClassic.TYPE_PRO;

public final class CardManager {
    //private static final String SP = "<br />------------------------------<br /><br />";
    private static final String SP = "<br />------------------------------</b><br />";

    public static String[][] TECHLISTS;
    public static IntentFilter[] FILTERS;

    static {
        try {
            TECHLISTS = new String[][]{{IsoDep.class.getName()}, {NfcV.class.getName()}, {NfcF.class.getName()},};
            FILTERS = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};
        } catch (Exception e) {
        }
    }

    public static String buildResult(String n, String i, String d, String x) {
        if (n == null)
            return null;

        final StringBuilder s = new StringBuilder();

        //s.append("<br/><b>").append(n).append("</b>");
        s.append(n);

        if (d != null)
            s.append(SP).append(d);

        if (x != null)
            s.append(SP).append(x);

        if (i != null)
            s.append(SP).append(i);

        return s.toString();
    }

    public static String load(Parcelable parcelable, Resources res) {
        final Tag tag = (Tag) parcelable;

        final NfcA nfcA = NfcA.get(tag);
        String sRet = readNfcA(nfcA);
        if (sRet.isEmpty() == false) {
            return sRet;
        }

        final IsoDep isodep = IsoDep.get(tag);
        Log.d("NFCTAG", "ffff");//isodep.transceive("45".getBytes()).toString());
        if (isodep != null) {
            return PbocCard.load(isodep, res);
        }

        final NfcV nfcv = NfcV.get(tag);
        if (nfcv != null) {
            return VicinityCard.load(nfcv, res);
        }

        final NfcF nfcf = NfcF.get(tag);
        if (nfcf != null) {
            return OctopusCard.load(nfcf, res);
        }
        return null;
    }

    public static String readNfcA(final NfcA nfca) {
        int mType = 0;
        int mSize = 0;
        boolean mIsEmulated = false;
        try {
            Short sak = nfca.getSak();
            switch (sak) {
                case 0x01:
                case 0x08:
                    mType = TYPE_CLASSIC;
                    mSize = SIZE_1K;
                    break;
                case 0x09:
                    mType = TYPE_CLASSIC;
                    mSize = SIZE_MINI;
                    break;
                case 0x10:
                    mType = TYPE_PLUS;
                    mSize = SIZE_2K;
                    break;
                case 0x11:
                    mType = TYPE_PLUS;
                    mSize = SIZE_4K;
                    break;
                case 0x18:
                    mType = TYPE_CLASSIC;
                    mSize = SIZE_4K;
                    break;
                case 0x28:
                    mType = TYPE_CLASSIC;
                    mSize = SIZE_1K;
                    mIsEmulated = true;
                    break;
                case 0x38:
                    mType = TYPE_CLASSIC;
                    mSize = SIZE_4K;
                    mIsEmulated = true;
                    break;
                case 0x88:
                    mType = TYPE_CLASSIC;
                    mSize = SIZE_1K;
                    break;
                case 0x98:
                case 0xB8:
                    mType = TYPE_PRO;
                    mSize = SIZE_4K;
                    break;
            }

            byte[] a = nfca.getAtqa();
            Log.d("NFCTAG", a.toString());
            String atqa_hex = Util.getHexString(a);
            byte[] uid = nfca.getTag().getId();
            String uid_hex = Util.getHexString(uid);
            Log.d("NFCTAG", "SAK = " + sak + "\nATQA = " + atqa_hex + "\nUID = " + uid_hex);
            nfca.close();
        } catch (IOException e) {
            Log.e("NFCTAG", "Error when reading tag");
        }

        return null;
    }

}
