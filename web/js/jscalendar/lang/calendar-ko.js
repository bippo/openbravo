// ** I18N

// Calendar EN language
// Author: Mihai Bazon, <mihai_bazon@yahoo.com>
// Translation: Yourim Yi <yyi@yourim.net>
// Encoding: EUC-KR
// lang : ko
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names

Calendar._DN = new Array
("ÀÏ¿äÀÏ",
 "¿ù¿äÀÏ",
 "È­¿äÀÏ",
 "¼ö¿äÀÏ",
 "¸ñ¿äÀÏ",
 "±Ý¿äÀÏ",
 "Åä¿äÀÏ",
 "ÀÏ¿äÀÏ");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("ÀÏ",
 "¿ù",
 "È­",
 "¼ö",
 "¸ñ",
 "±Ý",
 "Åä",
 "ÀÏ");

// full month names
Calendar._MN = new Array
("1¿ù",
 "2¿ù",
 "3¿ù",
 "4¿ù",
 "5¿ù",
 "6¿ù",
 "7¿ù",
 "8¿ù",
 "9¿ù",
 "10¿ù",
 "11¿ù",
 "12¿ù");

// short month names
Calendar._SMN = new Array
("1",
 "2",
 "3",
 "4",
 "5",
 "6",
 "7",
 "8",
 "9",
 "10",
 "11",
 "12");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "calendar ¿¡ ´ëÇØ¼­";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"\n"+
"ÃÖ½Å ¹öÀüÀ» ¹ÞÀ¸½Ã·Á¸é http://www.dynarch.com/projects/calendar/ ¿¡ ¹æ¹®ÇÏ¼¼¿ä\n" +
"\n"+
"GNU LGPL ¶óÀÌ¼¾½º·Î ¹èÆ÷µË´Ï´Ù. \n"+
"¶óÀÌ¼¾½º¿¡ ´ëÇÑ ÀÚ¼¼ÇÑ ³»¿ëÀº http://gnu.org/licenses/lgpl.html À» ÀÐÀ¸¼¼¿ä." +
"\n\n" +
"³¯Â¥ ¼±ÅÃ:\n" +
"- ¿¬µµ¸¦ ¼±ÅÃÇÏ·Á¸é \xab, \xbb ¹öÆ°À» »ç¿ëÇÕ´Ï´Ù\n" +
"- ´ÞÀ» ¼±ÅÃÇÏ·Á¸é " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " ¹öÆ°À» ´©¸£¼¼¿ä\n" +
"- °è¼Ó ´©¸£°í ÀÖÀ¸¸é À§ °ªµéÀ» ºü¸£°Ô ¼±ÅÃÇÏ½Ç ¼ö ÀÖ½À´Ï´Ù.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"½Ã°£ ¼±ÅÃ:\n" +
"- ¸¶¿ì½º·Î ´©¸£¸é ½Ã°£ÀÌ Áõ°¡ÇÕ´Ï´Ù\n" +
"- Shift Å°¿Í ÇÔ²² ´©¸£¸é °¨¼ÒÇÕ´Ï´Ù\n" +
"- ´©¸¥ »óÅÂ¿¡¼­ ¸¶¿ì½º¸¦ ¿òÁ÷ÀÌ¸é Á» ´õ ºü¸£°Ô °ªÀÌ º¯ÇÕ´Ï´Ù.\n";

Calendar._TT["PREV_YEAR"] = "Áö³­ ÇØ (±æ°Ô ´©¸£¸é ¸ñ·Ï)";
Calendar._TT["PREV_MONTH"] = "Áö³­ ´Þ (±æ°Ô ´©¸£¸é ¸ñ·Ï)";
Calendar._TT["GO_TODAY"] = "¿À´Ã ³¯Â¥·Î";
Calendar._TT["NEXT_MONTH"] = "´ÙÀ½ ´Þ (±æ°Ô ´©¸£¸é ¸ñ·Ï)";
Calendar._TT["NEXT_YEAR"] = "´ÙÀ½ ÇØ (±æ°Ô ´©¸£¸é ¸ñ·Ï)";
Calendar._TT["SEL_DATE"] = "³¯Â¥¸¦ ¼±ÅÃÇÏ¼¼¿ä";
Calendar._TT["DRAG_TO_MOVE"] = "¸¶¿ì½º µå·¡±×·Î ÀÌµ¿ ÇÏ¼¼¿ä";
Calendar._TT["PART_TODAY"] = " (¿À´Ã)";
Calendar._TT["MON_FIRST"] = "¿ù¿äÀÏÀ» ÇÑ ÁÖÀÇ ½ÃÀÛ ¿äÀÏ·Î";
Calendar._TT["SUN_FIRST"] = "ÀÏ¿äÀÏÀ» ÇÑ ÁÖÀÇ ½ÃÀÛ ¿äÀÏ·Î";
Calendar._TT["CLOSE"] = "´Ý±â";
Calendar._TT["TODAY"] = "¿À´Ã";
Calendar._TT["TIME_PART"] = "(Shift-)Å¬¸¯ ¶Ç´Â µå·¡±× ÇÏ¼¼¿ä";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%b/%e [%a]";

Calendar._TT["WK"] = "ÁÖ";
