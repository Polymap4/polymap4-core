/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.model;

/**
 * Commonly used web save fonts from
 * http://www.w3schools.com/cssref/css_websafe_fonts.asp
 *
 * @author Steffen Stundzig
 */
public enum FontFamily {
    georgia("Georgia, Serif"), palatino("Palatino Linotype, Book Antiqua, Palatino, Serif"), times(
            "Times New Roman, Times, Serif"), arial("Arial, Helvetica, SansSerif"), arialBlack(
                    "Arial Black, SansSerif"), comicSans("Comic Sans MS, SansSerif");

    private String value;


    FontFamily( final String value ) {
        this.value = value;
    }
    
    public String value() {
        return value;
    }
    
    public String[] families() {
        return value.split( "," );
    }
    
    /**
     * Abadi MT Condensed Extra Bold
Abadi MT Condensed Light
Adobe Arabic
Adobe Caslon Pro
Adobe Fan Heiti Std
Adobe Fangsong Std
Adobe Garamond Pro
Adobe Gothic Std
Adobe Hebrew
Adobe Heiti Std
Adobe Kaiti Std
Adobe Ming Std
Adobe Myungjo Std
Adobe Song Std
Al Bayan
Al Nile
Al Tarikh
American Typewriter
Andale Mono
Apple Braille
Apple Chancery
Apple Color Emoji
Apple SD Gothic Neo
Apple Symbols
AppleGothic
AppleMyungjo
Arial
Arial Black
Arial Hebrew
Arial Hebrew Scholar
Arial Narrow
Arial Rounded MT Bold
Arial Unicode MS
Athelas
Avenir
Avenir Next
Avenir Next Condensed
Ayuthaya
Baghdad
Bangla MN
Bangla Sangam MN
Baoli SC
Baskerville
Baskerville Old Face
Batang
Bauhaus 93
Beirut
Bell MT
Bernard MT Condensed
Big Caslon
Birch Std
Blackoak Std
Bodoni 72
Bodoni 72 Oldstyle
Bodoni 72 Smallcaps
Bodoni Ornaments
Book Antiqua
Bookman Old Style
Bookshelf Symbol 7
Bradley Hand
Braggadocio
Britannic Bold
Brush Script MT
Brush Script Std
Calibri
Calisto MT
Cambria
Cambria Math
Candara
Century
Century Gothic
Century Schoolbook
Chalkboard
Chalkboard SE
Chalkduster
Chaparral Pro
Charlemagne Std
Charter
Cochin
Colonna MT
Comic Sans MS
Consolas
Constantia
Cooper Black
Cooper Std
Copperplate
Copperplate Gothic Bold
Copperplate Gothic Light
Corbel
Corsiva Hebrew
Courier
Courier New
Curlz MT
Damascus
DecoType Naskh
Desdemona
Devanagari MT
Devanagari Sangam MN
Dialog
DialogInput
Didot
DIN Alternate
DIN Condensed
Diwan Kufi
Diwan Thuluth
Edwardian Script ITC
Engravers MT
Euphemia UCAS
Eurostile
Farah
Farisi
Footlight MT Light
Franklin Gothic Book
Franklin Gothic Medium
Futura
Futura Com
Futura LT Condensed
Gabriola
Garamond
GB18030 Bitmap
Geeza Pro
Geneva
Georgia
Giddyup Std
Gill Sans
Gill Sans MT
Gloucester MT Extra Condensed
Goudy Old Style
Gujarati MT
Gujarati Sangam MN
Gulim
GungSeo
Gurmukhi MN
Gurmukhi MT
Gurmukhi Sangam MN
Haettenschweiler
Hannotate SC
Hannotate TC
HanziPen SC
HanziPen TC
Harrington
HeadLineA
Heiti SC
Heiti TC
Helvetica
Helvetica Neue
Herculanum
Hiragino Kaku Gothic Pro
Hiragino Kaku Gothic ProN
Hiragino Kaku Gothic Std
Hiragino Kaku Gothic StdN
Hiragino Maru Gothic Pro
Hiragino Maru Gothic ProN
Hiragino Mincho Pro
Hiragino Mincho ProN
Hiragino Sans
Hiragino Sans GB
Hobo Std
Hoefler Text
Impact
Imprint MT Shadow
InaiMathi
Iowan Old Style
ITF Devanagari
ITF Devanagari Marathi
Kailasa
Kaiti SC
Kaiti TC
Kannada MN
Kannada Sangam MN
Kefa
Khmer MN
Khmer Sangam MN
Kino MT
Klee
Kohinoor Bangla
Kohinoor Devanagari
Kohinoor Telugu
Kokonor
Kozuka Gothic Pr6N
Kozuka Gothic Pro
Kozuka Mincho Pr6N
Kozuka Mincho Pro
Krungthep
KufiStandardGK
Lantinghei SC
Lantinghei TC
Lao MN
Lao Sangam MN
Letter Gothic Std
Libian SC
LiHei Pro
LiSong Pro
Lithos Pro
LotusWP Box
Lucida Blackletter
Lucida Bright
Lucida Calligraphy
Lucida Console
Lucida Fax
Lucida Grande
Lucida Handwriting
Lucida Sans
Lucida Sans Typewriter
Lucida Sans Unicode
Luminari
Malayalam MN
Malayalam Sangam MN
Marion
Marker Felt
Marlett
Matura MT Script Capitals
Meiryo
Menlo
Mesquite Std
Microsoft Himalaya
Microsoft Sans Serif
Microsoft Tai Le
Microsoft Yi Baiti
MingLiU
MingLiU-ExtB
MingLiU_HKSCS
MingLiU_HKSCS-ExtB
Minion Pro
Mishafi
Mishafi Gold
Mistral
Modern No. 20
Monaco
Mongolian Baiti
Monospaced
Monotype Corsiva
Monotype Sorts
MS Gothic
MS Mincho
MS PGothic
MS PMincho
MS Reference Sans Serif
MS Reference Specialty
Mshtakan
MT Extra
Muna
Myanmar MN
Myanmar Sangam MN
Myriad Pro
Nadeem
Nanum Brush Script
Nanum Gothic
Nanum Myeongjo
Nanum Pen Script
New Peninim MT
News Gothic MT
Noteworthy
Nueva Std
OCR A Std
Onyx
Optima
Orator Std
Oriya MN
Oriya Sangam MN
Osaka
Palatino
Palatino Linotype
Papyrus
PCMyungjo
Perpetua
Perpetua Titling MT
Phosphate
PilGi
PingFang HK
PingFang SC
PingFang TC
Plantagenet Cherokee
Playbill
PMingLiU
PMingLiU-ExtB
Poplar Std
Prestige Elite Std
PT Mono
PT Sans
PT Sans Caption
PT Sans Narrow
PT Serif
PT Serif Caption
Raanana
Rockwell
Rockwell Extra Bold
Rosewood Std
Sana
SansSerif
Sathu
Savoye LET
Seravek
Serif
Shree Devanagari 714
SignPainter
Silom
SimHei
SimSun
SimSun-ExtB
Sinhala MN
Sinhala Sangam MN
Skia
Snell Roundhand
Songti SC
Songti TC
StempelGaramond Roman
Stencil
Stencil Std
STFangsong
STHeiti
STIXGeneral
STIXIntegralsD
STIXIntegralsSm
STIXIntegralsUp
STIXIntegralsUpD
STIXIntegralsUpSm
STIXNonUnicode
STIXSizeFiveSym
STIXSizeFourSym
STIXSizeOneSym
STIXSizeThreeSym
STIXSizeTwoSym
STIXVariants
STKaiti
STSong
Sukhumvit Set
Superclarendon
Symbol
Tahoma
Tamil MN
Tamil Sangam MN
TeamViewer10
Tekton Pro
Telugu MN
Telugu Sangam MN
Thonburi
Times
Times New Roman
Trajan Pro
Trattatello
Trebuchet MS
Tsukushi A Round Gothic
Tsukushi B Round Gothic
Tw Cen MT
Verdana
Waseem
Wawati SC
Wawati TC
Webdings
Weibei SC
Weibei TC
Wide Latin
Wingdings
Wingdings 2
Wingdings 3
Xingkai SC
Yuanti SC
Yuanti TC
YuGothic
YuMincho
YuMincho +36p Kana
Yuppy SC
Yuppy TC
Zapf Dingbats
Zapfino

     */
}
