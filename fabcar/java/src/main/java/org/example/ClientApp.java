/*
SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.amazonaws.services.s3.model.*;
import com.google.errorprone.annotations.DoNotCall;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.*;
import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PersonalKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PublicKey;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.SecretKey;

public class ClientApp {

    final static String[] medRecordIds = {"1614632807622fuaMa", "1614632807622gMqfZ", "1614632807624EFZfb", "1614632807626nzoA8", "1614632807627Y0Ipp", "1614632807629Xigyp", "1614632807632n6K9B", "1614632807633K+lwP", "1614632807633Uyppg", "1614632807634L+oLq", "1614632807637teEbE", "1614632808148jcvmS", "1614632808153JwkuH", "1614632808172ggcqm", "1614632808176UT40k", "1614632808185EIUfT", "1614632808185TOfoj", "16146328081895MK3C", "1614632808189CGhoL", "161463280820506Ohf", "1614632808595YJ6Sg", "16146328086019umId", "1614632808714uT2hL", "1614632808719lHER/", "1614632808720JoiDg", "1614632808721C026P", "1614632808736loh74", "1614632808744zVPfv", "1614632808797R71Eb", "1614632809278tFIWn", "161463281018509r4G", "16146328102868Cg+x", "1614632810336up4lh", "1614632810343mxtO0", "1614632810361HXayz", "1614632810376rtQ0v", "1614632810377uFz31", "1614632810511rdv6E", "1614632810760UtwqY", "1614632810780egVUB", "1614632810908EHtOF", "1614632810912AJ0Zh", "1614632810948pWO3z", "1614632810958mXhle", "1614632810970yfSxF", "16146328109877wKim", "1614632810993KFTcz", "16146328109999/76V", "1614632810999gNUu8", "1614632811019Lw7vj", "1614632812302OEEdb", "161463281237670VT8", "1614632812382GJZPI", "1614632812395WL6UL", "1614632812412Pces6", "1614632812470BXSkb", "1614632812470gt4sg", "1614632812473iR0UT", "1614632812498f0KN7", "1614632812531iuyBe", "16146328125905tzbM", "1614632812618xMCPB", "1614632812624MmsHL", "1614632812628Jsj+G", "1614632812636Rw6kr", "16146328126372kajW", "1614632812651cgZ1e", "1614632812657MlmOH", "16146328126684p59s", "1614632812683QC5Vi", "1614632813338S08Gx", "1614632813344DvITg", "16146328135498i2rA", "1614632813920j/xxS", "1614632813938a1c9N", "1614632813974OcZel", "1614632813995RHQTH", "1614632814105cQ14h", "1614632814117ykcxF", "1614632814248Px5ND", "1614632814745pgnec", "16146328147762lttm", "1614632814896+pK/w", "1614632814911jYgFx", "1614632814996ZeWJp", "1614632815028XjZ6C", "1614632815052/Zm5S", "1614632815061ElqGW", "1614632815074wrX1M", "1614632815088r4oJc", "16146328151036pZx0", "16146328151572DMQs", "1614632816386FzzRi", "16146328163954chTV", "1614632816399fTADa", "1614632816462PKArC", "1614632816545upoax", "1614632816564ZPHMu", "1614632816566qEs73", "1614632816578Nw0lV", "1614632816582+K6H7", "1614632816614p1Bws", "16146328166345ewz6", "1614632816732T5L3H", "16146328168211NfX/", "1614632816826C8rKg", "1614632816833MsewF", "16146328168667iapT", "1614632816904qdK9Q", "1614632816912k6BDE", "1614632816918oA//s", "1614632816943iUNXO", "16146328170285RwJA", "1614632817098qZcsj", "1614632817104/n0bL", "1614632817111XtrTP", "1614632817200xlnmx", "1614632817211NBERV", "1614632817228wN1VU", "1614632817236Cs72W", "1614632817241e+Ws4", "1614632817250RIbud", "1614632817251Ebnn4", "1614632817255Dn6Ia", "1614632817256QIO/3", "1614632817257npT+n", "1614632817266yFtIO", "1614632817270LpBrJ", "1614632817274gl5bM", "1614632817280ONjkv", "16146328185318DWe2", "1614632818533kGavV", "1614632818560er2wr", "1614632818560YZmfr", "1614632818567Db3Ni", "1614632818583WKRYh", "1614632818584UaoO+", "161463281879417A08", "1614632818823vcFDS", "16146328188276OiOb", "1614632819793vT8Nw", "1614632819841wpgdr", "16146328198652oq2i", "1614632819865oPFfF", "1614632819892yH0IA", "1614632819913g6y6G", "16146328199189v8iV", "1614632819943kIMqU", "1614632819946rtpSd", "16146328199555awmh", "1614632820370rXAqY", "1614632820413TMjPH", "1614632820414FmRKV", "1614632820430Ii7AB", "1614632820543GWmgn", "1614632820562yvxG8", "16146328205986NHgG", "1614632820675Cfb8D", "1614632820729/hmfc", "1614632820957H2qnD", "1614632820973g7Zay", "1614632821081qTNod", "1614632821163EcJZq", "1614632821174Em2GU", "1614632821222KSCvB", "1614632821311/eYce", "1614632821458PMEaB", "1614632821498CoEeq", "1614632821575T0ZTW", "16146328216498uy0a", "1614632821816O3Mue", "1614632821878l68G8", "1614632821970U6k+3", "1614632821976iwWJN", "1614632822000+TGAE", "1614632822017a1YyF", "1614632822024IFpMs", "1614632822052C2HAG", "161463282206859D7U", "1614632822238R5wx3", "1614632822590ecDJh", "1614632822640yKO1O", "1614632822665UMP0u", "1614632822774G6eKw", "1614632822813CNGH3", "1614632822856HtBBt", "1614632822858bJ7iP", "1614632822877NXBKY", "1614632822883x+Bg3", "1614632822963IyOBJ", "1614632823018qHwhc", "1614632823794/OLXU", "1614632823794Q4gIw", "1614632823833cqx2N", "1614632823922BMO5g", "1614632824046L5U6a", "161463282833505n2X", "1614632828338zjtK8", "1614632828339T4dQX", "16146328283987ahdU", "1614632828413R2Wwy", "1614632828422f7Aj6", "1614632828429Qttfo", "1614632834861sEpg2", "1614632834862HVX2x", "1614632834866xW2ia", "1614632834882gnRma", "1614632834898x8dWE", "16146328351434+BO8", "1614632835200bPQcg", "1614632837162NY/Mi", "16146328371723VxsD", "1614632837196iFo/O", "16146328371981j+ok", "16146328372150OVuL", "1614632837274t1x/U", "1614632837392fY6da", "1614632837416cPZ0q", "1614632837440y7sY9", "1614632837450FF6Xs", "16146328382591/VbI", "1614632838366RnA64", "1614632838370NsX1S", "1614632838373I/QpM", "1614632838400M9KQs", "1614632838413Cpz4D", "1614632838414tpDuf", "1614632838438YXPfL", "1614632838494xQtNO", "161463283878618PDn", "16146328388439ipL8", "1614632838851CHf/o", "1614632838869HFG36", "16146328389061lHUD", "1614632838908V4FCU", "1614632838932slJJB", "1614632838933YIHjn", "1614632838952JY92m", "1614632838957oqbZs", "1614632838973uHCy5", "1614632838975Y4r9o", "16146328389766GKpw", "1614632838979W9r2K", "1614632838979wr0HC", "16146328389840oWpe", "1614632838985l1Og0", "1614632838988FOBGe", "1614632838993I2pEE", "1614632839000xwt7F", "1614632839018VP+3u", "1614632845562eA1O0", "1614632845586JdZL2", "1614632845590jp/MX", "1614632845595o5OJN", "1614632845612b2xe3", "1614632845631ME0f4", "1614632845671bWe/0", "1614632845687fNwpt", "1614632846170mwJbF", "1614632851408xuIQh", "1614632852203NbALo", "1614632852228JjSSf", "1614632852251CcmqO", "1614632852251IVGDM", "1614632852268Dl7MT", "1614632852276izuZR", "16146328522831Xi00", "1614632852289z5XBP", "1614632852297dPc18", "1614632852297mdH1W", "1614632853294LTYVF", "1614632853360ad/MX", "1614632853371Rei30", "161463285338545StB", "1614632853387S7WCa", "1614632853418EtzVx", "1614632853418Zp758", "1614632853432iWoYa", "1614632853459xALq+", "1614632853490HExeV", "1614632854473D/F3U", "1614632854519ySjxT", "1614632854653OFW7d", "1614632854662CCig3", "1614632854668S1DnR", "1614632854676NHwSq", "1614632854708n4ddi", "1614632854709as90y", "1614632854717QZ1KU", "1614632854747dEQC+", "16146328583561XNI2", "161463285839560phs", "1614632858408HRJ3O", "1614632858409rdu64", "1614632858552/81q4", "16146328585545bTWp", "1614632858557A9krB", "1614632858569yVgQG", "16146328585783BBcJ", "1614632858579xSbZV", "1614632860326Er7kk", "1614632860337wyy5w", "1614632861041QO/Q+", "1614632861061fOvnI", "1614632861064HzSkB", "1614632861065HAyGb", "1614632861069DbxVL", "16146328610747pu0G", "1614632861078/qdnG", "1614632861105ESmwb", "1614632868445xZBud", "1614632868535/MBrm", "16146328688682pSny", "16146328688879BD4X", "1614632868892Y6vfI", "1614632868903EEK5s", "1614632868914a8Juh", "1614632868915QwCfy", "1614632868956J13LK", "1614632869070uJfZI", "1614632873886yylS+", "1614632873932aYudO", "1614632873953MemrE", "16146328740151lv/K", "1614632874037TghLl", "1614632874038XSlxW", "1614632874051vjOiA", "16146328741216d5bs", "1614632874121JbhqX", "1614632874129fl35a", "1614632877960lkrwI", "16146328779737aZ46", "1614632878061ZjVsc", "1614632878089Ble/T", "1614632878090GC26Z", "1614632878111OrqyY", "1614632878121JLbVX", "16146328781237zMCe", "16146328781518THFv", "1614632878166etVvd", "1614632879325e35KX", "1614632879326DkxzC", "1614632879370jp0+w", "1614632879371w390y", "1614632879434vaTRM", "1614632879455jJym6", "1614632879476AtE7B", "1614632879477jU+rS", "1614632879479hXAC6", "1614632879506tMm1/", "1614632880495cWwpH", "1614632880515G5xDG", "1614632880524QVk7W", "1614632880557TMEFI", "1614632880571kw0X2", "16146328805842Dspt", "1614632880611rScoa", "1614632880620WWVt/", "1614632880622o8Le9", "1614632880635KGsId", "16146328820696VKv6", "1614632882108QPdl3", "1614632882139zDd2y", "1614632882198XmWec", "1614632882209VE86Q", "1614632882217+yDZD", "16146328822287YLDN", "1614632882260r5sCB", "1614632882294WaT6P", "1614632882323SzUPo", "1614632884797Uh78u", "1614632884825vqIVC", "161463288483200ROP", "16146328848360Xi8O", "1614632884863jVxFC", "16146328848755sUPF", "1614632884885ld7Cs", "1614632884887bef3t", "1614632884907y5TxV", "1614632884920ePABf", "1614632885936nH9/n", "1614632886081Ipwlt", "1614632886127IsUmQ", "161463288613249GON", "1614632886156UIW1O", "1614632886172YuVVC", "1614632886353z/h6B", "1614632886354JgSzR", "1614632886356otp5g", "1614632886407y9iY/", "1614632887625q5+V2", "1614632887632hcSck", "1614632887632ZKs1e", "1614632887634aniap", "1614632887635LTKUd", "1614632887655RH/Hz", "1614632887655y9wjm", "1614632887667laopq", "1614632887688sDjjv", "1614632890412uUq0a", "1614632890498QH5/R", "1614632890561Gouqr", "1614632890562p3OeV", "1614632890564/olQ7", "1614632890569/VW1/", "1614632890593fdc4Z", "1614632890611Y92JI", "1614632890612xFI2s", "1614632890614ce++K", "1614632890926/4yFd", "161463289145568+yI", "1614632891577Tkt7q", "1614632891628xDlFh", "1614632891649LUAFn", "1614632891679oAHli", "16146328916808+Rw6", "1614632891689BqU9n", "1614632891691lcu2d", "161463289169886rdO", "1614632891702gWQi/", "1614632897336PA3em", "1614632897522wdkaw", "1614632897596ULDzR", "1614632897603CbcNn", "1614632897610Vk+Ly", "1614632897611YF6Ok", "1614632897613dEBJg", "1614632897621aYnxb", "1614632897627wpE+A", "1614632897708+1k55", "1614632898585z0vqX", "1614632898656FkcoE", "1614632898681nHtns", "16146328986895SBuN", "1614632898699O5yDq", "1614632898703HY8RY", "1614632899345k9Fms", "1614632899349uc/SE", "1614632899379Xlwyo", "1614632899423agHJN", "1614632899465IOZwY", "16146328995503KVXg", "1614632899592Soqw8", "1614632899593ZhJtk", "1614632899633jBJ0m", "1614632899666FZX2T", "1614632900056Tyvv9", "1614632900062wGEDy", "1614632900075U6DsU", "1614632900092XfB4I", "1614632900303jYEYE", "1614632900569ZqHUG", "1614632900590GgRH+", "1614632900590W9zX2", "1614632900592cSAAI", "1614632900614lwu0q", "1614632900639u3ujz", "1614632900641qShMg", "1614632900641Z9BVu", "1614632900673u/BI1", "1614632901361vFixd", "1614632901395/s9C6", "1614632901448yLMMZ", "1614632901473BlxMx", "1614632901503hBi8V", "16146329015047EyCR", "16146329015292SvaF", "161463290153375qMo", "1614632901536AS7rT", "16146329015437SJwu", "1614632901731t1gAA", "1614632901737phn4/", "1614632901739hdmIY", "1614632901742NP51/", "1614632901746CB9Xe", "1614632901788yn5F+", "1614632901789und0L", "1614632901987Cov9M", "1614632902041TRl86", "1614632902071Degcf", "1614632904474bGGeL", "1614632904512ECUSZ", "1614632904535ycIGk", "1614632904575Eu87e", "161463290465084x86", "1614632904654IpyYl", "1614632904659Pv9rq", "1614632904673u1n5h", "1614632904678M14+T", "1614632904683zM3ey", "1614632906204vlYAw", "1614632906582XVL0j", "1614632906608x+qCc", "1614632906618hOgHJ", "1614632906619wU03i", "16146329067020oHb9", "1614632906709ep5B3", "1614632906723neF7R", "1614632907592i89rg", "16146329075972TuVo", "16146329084621ykwa", "1614632908485pA4wh", "1614632908522ow7Iy", "1614632908578UIrOO", "1614632908611GXqAA", "1614632908627VPvtb", "1614632908638ndP0L", "1614632908639oV+5s", "1614632908657RD77B", "1614632908665I3aN6", "1614632910390A1Y3V", "1614632910417Ckala", "1614632910444EptlI", "1614632910466y62Ly", "1614632910476sGQqm", "161463291053415hE+", "1614632910542ECNls", "16146329105578yYX+", "1614632910586nlu0w", "1614632910597wBDk4", "1614632911219yVe1K", "1614632911235NPGqc", "1614632911243FrAEn", "1614632911246Nfi4i", "16146329112485DslX", "1614632911266ySnx7", "1614632911276vj3at", "1614632911832Pn7eN", "1614632911956TpA+d", "16146329120201ihZL", "1614632912611h2erT", "1614632912617r3md2", "1614632912624XPnUd", "1614632912634BXiOS", "1614632912641nwuON", "1614632912650oVtrI", "1614632913101VscDd", "1614632913104Nm+Bn", "1614632913142Bfi75", "16146329133293QORc", "1614632915822OxEwK", "1614632915827SPbrS", "1614632916050wT+lP", "1614632916208Uh0Zu", "1614632916246OhIcA", "1614632916247MBKs/", "161463291624826FCe", "1614632916249LKfWj", "161463291627543X/U", "1614632916282rIN/1", "1614632919388Ms28S", "1614632919406YqEXN", "1614632919446lEctc", "1614632919471MsPJf", "1614632919511KRBYS", "16146329195148069Z", "1614632920109opm2w", "1614632920128kuAYq", "1614632920130wDjbs", "1614632920136ILbtQ", "1614632920249XIwXt", "1614632920347D4vzg", "1614632920389wVSbf", "1614632920404TVXjA", "1614632920443cKj9y", "1614632920454udQTa", "1614632920458TWweV", "1614632920472adSS9", "1614632920612ZthRC", "1614632920614jZoG3", "1614632923850M50kP", "1614632923872t2MS/", "1614632923876xSn17", "1614632923897JV6is", "16146329239059PQe7", "1614632923910k5qXh", "1614632924876duFor", "1614632924923mOBZf", "1614632924960P3FPM", "1614632924969hWphH", "161463292571899NYo", "1614632925725jitV8", "161463292573025TS9", "1614632925732KeIxA", "1614632925947UNWXM", "1614632925950OMHbW", "1614632925965bPSqK", "1614632925974SVsd0", "16146329266798M/U8", "16146329267648sSDk", "1614632926806tOQDZ", "1614632926913HOG5I", "1614632926977U91J5", "1614632926994eHZIW", "1614632926996sgQ4H", "1614632927000Nn5Vu", "1614632927005+BCVm", "1614632927015NxvGo", "1614632927032iEqhC", "1614632927036SuU/t", "1614632929133R/Qu3", "1614632929176Q1nWB", "1614632929179V2tPq", "1614632929195tpmRP", "1614632929229kUwKS", "1614632929231Ty74T", "1614632929238IbVSe", "1614632929243GR4Ic", "1614632929962hKtnL", "1614632930028VgF4f", "161463293035493sRV", "1614632930377u0xTA", "16146329303867nE4a", "1614632930387hUplw", "1614632930402z+GQk", "16146329304107fRAz", "1614632932209lFQ/3", "1614632932210ORw6n", "16146329322161ks5e", "1614632932243bnNF+", "1614632932906iXBCf", "16146329329412AdlR", "16146329329849+oJ7", "1614632932996VcNql", "1614632933012CNYip", "1614632933050fnHTp", "1614632933101/iLH1", "1614632933120QBCC4", "1614632933121WlzWG", "1614632933135FzoVn", "1614632934107xj9se", "16146329342097KGj8", "1614632934262UC9Pb", "1614632934285gAh7u", "1614632934291mXofE", "1614632934293qpgZJ", "1614632934294DYR0Y", "16146329343263lQeV", "1614632934972PdNH2", "1614632934975fYcS2", "1614632935260BFEV0", "1614632935311nbWqh", "1614632935312Pa50N", "1614632935750BEQ0E", "1614632935784oekHu", "1614632935786w4auK", "1614632935796poSA8", "16146329357978B/B9", "1614632935854wBC9H", "161463293589116L/F", "1614632936109fqUiX", "1614632936149h3ppZ", "1614632936157iaAFY", "1614632936170NWu1u", "1614632936201dREDX", "1614632936201XvV+k", "1614632936206PlmBN", "1614632936208uvPKG", "16146329362551N/do", "1614632936265vLveU", "1614632937479AjkHv", "16146329377158nQIa", "1614632937715fr8oI", "1614632937721zn6H6", "1614632937729f1R2z", "1614632937734lVOmt", "1614632937738qT17L", "16146329377408QIjy", "1614632938042IY1xA", "1614632938054Xag/T", "1614632938551b0/wx", "1614632938558udtRK", "1614632938635MgjG7", "16146329386870ITFy", "1614632938719k0/Pq", "1614632938731msZl/", "1614632938777j4wVV", "1614632938803AZ+c4", "1614632938814cj2Fn", "1614632939148JrDh3", "1614632939239A8Fxu", "16146329392640BihE", "1614632939266NvkA+", "1614632939308b+Kt4", "16146329393187Gcqb", "161463293933695OwD", "1614632939338hx5iS", "1614632939345MWVP9", "1614632939346K4E0l", "1614632939389I36a0", "1614632939442Wcs8U", "1614632939523ZFhQR", "1614632939530tp3OR", "1614632940821GdAIG", "1614632940882HOTew", "1614632940913JcsQK", "1614632940922QhZwE", "1614632940926XyB8J", "1614632940943apapq", "1614632940959l1BCv", "1614632941083Ff4u9", "1614632941245B7mY5", "1614632941260M85BD", "1614632941271X7Iwm", "1614632941298A4LW3", "1614632941306T68eL", "16146329413094hxGk", "1614632941310Q5j/V", "1614632941315Rh8Z4", "1614632941683m/9sj", "1614632942042FeVC4", "1614632942123pH6Zv", "1614632942126fKi0F", "1614632942151GgblJ", "1614632942156Yl9W3", "1614632942163Hz/O4", "1614632942182yGZL2", "1614632942183CEweX", "1614632942185Y1PVK", "16146329422019goxs", "1614632942893TWuXO", "1614632942938LO/jX", "1614632942940K0Dqw", "1614632942947S0GVh", "1614632942962vkv0H", "1614632943003SYSlk", "16146329430131HVrG", "1614632943014D/ku9", "16146329430243p9Ku", "1614632943052YwzRY", "1614632943476GHVhD", "1614632943480llJlw", "1614632943510/s14O", "1614632943513Gkbwh", "1614632943530pKhjd", "1614632943543iK11X", "1614632943558M/uij", "1614632943582Peudk", "1614632943587Qg+vR", "16146329435885EeWX", "1614632946728KmkN9", "1614632946758XrGIW", "1614632946822ZglJY", "1614632946823T2b5v", "1614632946824UItio", "1614632946838qBi75", "16146329478523FhYM", "1614632947894lwl88", "1614632947897aJReC", "1614632947915mcEQ2"};
    final static String[] userIds = {"medic_user_parallel_test_query__1", "medic_user_parallel_test_query__10", "medic_user_parallel_test_query__100", "medic_user_parallel_test_query__101", "medic_user_parallel_test_query__102", "medic_user_parallel_test_query__103", "medic_user_parallel_test_query__104", "medic_user_parallel_test_query__105", "medic_user_parallel_test_query__106", "medic_user_parallel_test_query__107", "medic_user_parallel_test_query__108", "medic_user_parallel_test_query__109", "medic_user_parallel_test_query__11", "medic_user_parallel_test_query__110", "medic_user_parallel_test_query__111", "medic_user_parallel_test_query__112", "medic_user_parallel_test_query__113", "medic_user_parallel_test_query__114", "medic_user_parallel_test_query__115", "medic_user_parallel_test_query__116", "medic_user_parallel_test_query__117", "medic_user_parallel_test_query__118", "medic_user_parallel_test_query__119", "medic_user_parallel_test_query__12", "medic_user_parallel_test_query__120", "medic_user_parallel_test_query__121", "medic_user_parallel_test_query__122", "medic_user_parallel_test_query__123", "medic_user_parallel_test_query__124", "medic_user_parallel_test_query__125", "medic_user_parallel_test_query__126", "medic_user_parallel_test_query__127", "medic_user_parallel_test_query__128", "medic_user_parallel_test_query__129", "medic_user_parallel_test_query__13", "medic_user_parallel_test_query__130", "medic_user_parallel_test_query__131", "medic_user_parallel_test_query__132", "medic_user_parallel_test_query__133", "medic_user_parallel_test_query__134", "medic_user_parallel_test_query__135", "medic_user_parallel_test_query__136", "medic_user_parallel_test_query__137", "medic_user_parallel_test_query__138", "medic_user_parallel_test_query__139", "medic_user_parallel_test_query__14", "medic_user_parallel_test_query__140", "medic_user_parallel_test_query__141", "medic_user_parallel_test_query__142", "medic_user_parallel_test_query__143", "medic_user_parallel_test_query__144", "medic_user_parallel_test_query__145", "medic_user_parallel_test_query__146", "medic_user_parallel_test_query__147", "medic_user_parallel_test_query__148", "medic_user_parallel_test_query__149", "medic_user_parallel_test_query__15", "medic_user_parallel_test_query__150", "medic_user_parallel_test_query__151", "medic_user_parallel_test_query__152", "medic_user_parallel_test_query__153", "medic_user_parallel_test_query__154", "medic_user_parallel_test_query__155", "medic_user_parallel_test_query__156", "medic_user_parallel_test_query__157", "medic_user_parallel_test_query__158", "medic_user_parallel_test_query__159", "medic_user_parallel_test_query__16", "medic_user_parallel_test_query__160", "medic_user_parallel_test_query__161", "medic_user_parallel_test_query__162", "medic_user_parallel_test_query__163", "medic_user_parallel_test_query__164", "medic_user_parallel_test_query__165", "medic_user_parallel_test_query__166", "medic_user_parallel_test_query__167", "medic_user_parallel_test_query__168", "medic_user_parallel_test_query__169", "medic_user_parallel_test_query__17", "medic_user_parallel_test_query__170", "medic_user_parallel_test_query__171", "medic_user_parallel_test_query__172", "medic_user_parallel_test_query__173", "medic_user_parallel_test_query__174", "medic_user_parallel_test_query__175", "medic_user_parallel_test_query__176", "medic_user_parallel_test_query__177", "medic_user_parallel_test_query__178", "medic_user_parallel_test_query__179", "medic_user_parallel_test_query__18", "medic_user_parallel_test_query__180", "medic_user_parallel_test_query__181", "medic_user_parallel_test_query__182", "medic_user_parallel_test_query__183", "medic_user_parallel_test_query__184", "medic_user_parallel_test_query__185", "medic_user_parallel_test_query__186", "medic_user_parallel_test_query__187", "medic_user_parallel_test_query__188", "medic_user_parallel_test_query__189", "medic_user_parallel_test_query__19", "medic_user_parallel_test_query__190", "medic_user_parallel_test_query__191", "medic_user_parallel_test_query__192", "medic_user_parallel_test_query__193", "medic_user_parallel_test_query__194", "medic_user_parallel_test_query__195", "medic_user_parallel_test_query__196", "medic_user_parallel_test_query__197", "medic_user_parallel_test_query__198", "medic_user_parallel_test_query__199", "medic_user_parallel_test_query__2", "medic_user_parallel_test_query__20", "medic_user_parallel_test_query__200", "medic_user_parallel_test_query__201", "medic_user_parallel_test_query__202", "medic_user_parallel_test_query__203", "medic_user_parallel_test_query__204", "medic_user_parallel_test_query__205", "medic_user_parallel_test_query__206", "medic_user_parallel_test_query__207", "medic_user_parallel_test_query__208", "medic_user_parallel_test_query__209", "medic_user_parallel_test_query__21", "medic_user_parallel_test_query__210", "medic_user_parallel_test_query__211", "medic_user_parallel_test_query__212", "medic_user_parallel_test_query__213", "medic_user_parallel_test_query__214", "medic_user_parallel_test_query__215", "medic_user_parallel_test_query__216", "medic_user_parallel_test_query__217", "medic_user_parallel_test_query__218", "medic_user_parallel_test_query__219", "medic_user_parallel_test_query__22", "medic_user_parallel_test_query__220", "medic_user_parallel_test_query__221", "medic_user_parallel_test_query__222", "medic_user_parallel_test_query__223", "medic_user_parallel_test_query__224", "medic_user_parallel_test_query__225", "medic_user_parallel_test_query__226", "medic_user_parallel_test_query__227", "medic_user_parallel_test_query__228", "medic_user_parallel_test_query__229", "medic_user_parallel_test_query__23", "medic_user_parallel_test_query__230", "medic_user_parallel_test_query__231", "medic_user_parallel_test_query__232", "medic_user_parallel_test_query__233", "medic_user_parallel_test_query__234", "medic_user_parallel_test_query__235", "medic_user_parallel_test_query__236", "medic_user_parallel_test_query__237", "medic_user_parallel_test_query__238", "medic_user_parallel_test_query__239", "medic_user_parallel_test_query__24", "medic_user_parallel_test_query__240", "medic_user_parallel_test_query__241", "medic_user_parallel_test_query__242", "medic_user_parallel_test_query__243", "medic_user_parallel_test_query__244", "medic_user_parallel_test_query__245", "medic_user_parallel_test_query__246", "medic_user_parallel_test_query__247", "medic_user_parallel_test_query__248", "medic_user_parallel_test_query__249", "medic_user_parallel_test_query__25", "medic_user_parallel_test_query__250", "medic_user_parallel_test_query__251", "medic_user_parallel_test_query__252", "medic_user_parallel_test_query__253", "medic_user_parallel_test_query__254", "medic_user_parallel_test_query__255", "medic_user_parallel_test_query__256", "medic_user_parallel_test_query__257", "medic_user_parallel_test_query__258", "medic_user_parallel_test_query__259", "medic_user_parallel_test_query__26", "medic_user_parallel_test_query__260", "medic_user_parallel_test_query__261", "medic_user_parallel_test_query__262", "medic_user_parallel_test_query__263", "medic_user_parallel_test_query__264", "medic_user_parallel_test_query__265", "medic_user_parallel_test_query__266", "medic_user_parallel_test_query__267", "medic_user_parallel_test_query__268", "medic_user_parallel_test_query__269", "medic_user_parallel_test_query__27", "medic_user_parallel_test_query__270", "medic_user_parallel_test_query__271", "medic_user_parallel_test_query__272", "medic_user_parallel_test_query__273", "medic_user_parallel_test_query__274", "medic_user_parallel_test_query__275", "medic_user_parallel_test_query__276", "medic_user_parallel_test_query__277", "medic_user_parallel_test_query__278", "medic_user_parallel_test_query__279", "medic_user_parallel_test_query__28", "medic_user_parallel_test_query__280", "medic_user_parallel_test_query__281", "medic_user_parallel_test_query__282", "medic_user_parallel_test_query__283", "medic_user_parallel_test_query__284", "medic_user_parallel_test_query__285", "medic_user_parallel_test_query__286", "medic_user_parallel_test_query__287", "medic_user_parallel_test_query__288", "medic_user_parallel_test_query__289", "medic_user_parallel_test_query__29", "medic_user_parallel_test_query__290", "medic_user_parallel_test_query__291", "medic_user_parallel_test_query__292", "medic_user_parallel_test_query__293", "medic_user_parallel_test_query__294", "medic_user_parallel_test_query__295", "medic_user_parallel_test_query__296", "medic_user_parallel_test_query__297", "medic_user_parallel_test_query__298", "medic_user_parallel_test_query__299", "medic_user_parallel_test_query__3", "medic_user_parallel_test_query__30", "medic_user_parallel_test_query__300", "medic_user_parallel_test_query__301", "medic_user_parallel_test_query__302", "medic_user_parallel_test_query__303", "medic_user_parallel_test_query__304", "medic_user_parallel_test_query__305", "medic_user_parallel_test_query__306", "medic_user_parallel_test_query__307", "medic_user_parallel_test_query__308", "medic_user_parallel_test_query__309", "medic_user_parallel_test_query__31", "medic_user_parallel_test_query__310", "medic_user_parallel_test_query__311", "medic_user_parallel_test_query__312", "medic_user_parallel_test_query__313", "medic_user_parallel_test_query__314", "medic_user_parallel_test_query__315", "medic_user_parallel_test_query__316", "medic_user_parallel_test_query__317", "medic_user_parallel_test_query__318", "medic_user_parallel_test_query__319", "medic_user_parallel_test_query__32", "medic_user_parallel_test_query__320", "medic_user_parallel_test_query__321", "medic_user_parallel_test_query__322", "medic_user_parallel_test_query__323", "medic_user_parallel_test_query__324", "medic_user_parallel_test_query__325", "medic_user_parallel_test_query__326", "medic_user_parallel_test_query__327", "medic_user_parallel_test_query__328", "medic_user_parallel_test_query__329", "medic_user_parallel_test_query__33", "medic_user_parallel_test_query__330", "medic_user_parallel_test_query__331", "medic_user_parallel_test_query__332", "medic_user_parallel_test_query__333", "medic_user_parallel_test_query__334", "medic_user_parallel_test_query__335", "medic_user_parallel_test_query__336", "medic_user_parallel_test_query__337", "medic_user_parallel_test_query__338", "medic_user_parallel_test_query__339", "medic_user_parallel_test_query__34", "medic_user_parallel_test_query__340", "medic_user_parallel_test_query__341", "medic_user_parallel_test_query__342", "medic_user_parallel_test_query__343", "medic_user_parallel_test_query__344", "medic_user_parallel_test_query__345", "medic_user_parallel_test_query__346", "medic_user_parallel_test_query__347", "medic_user_parallel_test_query__348", "medic_user_parallel_test_query__349", "medic_user_parallel_test_query__35", "medic_user_parallel_test_query__350", "medic_user_parallel_test_query__351", "medic_user_parallel_test_query__352", "medic_user_parallel_test_query__353", "medic_user_parallel_test_query__354", "medic_user_parallel_test_query__355", "medic_user_parallel_test_query__356", "medic_user_parallel_test_query__357", "medic_user_parallel_test_query__358", "medic_user_parallel_test_query__359", "medic_user_parallel_test_query__36", "medic_user_parallel_test_query__360", "medic_user_parallel_test_query__361", "medic_user_parallel_test_query__362", "medic_user_parallel_test_query__363", "medic_user_parallel_test_query__364", "medic_user_parallel_test_query__365", "medic_user_parallel_test_query__366", "medic_user_parallel_test_query__367", "medic_user_parallel_test_query__368", "medic_user_parallel_test_query__369", "medic_user_parallel_test_query__37", "medic_user_parallel_test_query__370", "medic_user_parallel_test_query__371", "medic_user_parallel_test_query__372", "medic_user_parallel_test_query__373", "medic_user_parallel_test_query__374", "medic_user_parallel_test_query__375", "medic_user_parallel_test_query__376", "medic_user_parallel_test_query__377", "medic_user_parallel_test_query__378", "medic_user_parallel_test_query__379", "medic_user_parallel_test_query__38", "medic_user_parallel_test_query__380", "medic_user_parallel_test_query__381", "medic_user_parallel_test_query__382", "medic_user_parallel_test_query__383", "medic_user_parallel_test_query__384", "medic_user_parallel_test_query__385", "medic_user_parallel_test_query__386", "medic_user_parallel_test_query__387", "medic_user_parallel_test_query__388", "medic_user_parallel_test_query__389", "medic_user_parallel_test_query__39", "medic_user_parallel_test_query__390", "medic_user_parallel_test_query__391", "medic_user_parallel_test_query__392", "medic_user_parallel_test_query__393", "medic_user_parallel_test_query__394", "medic_user_parallel_test_query__395", "medic_user_parallel_test_query__396", "medic_user_parallel_test_query__397", "medic_user_parallel_test_query__398", "medic_user_parallel_test_query__399", "medic_user_parallel_test_query__4", "medic_user_parallel_test_query__40", "medic_user_parallel_test_query__400", "medic_user_parallel_test_query__401", "medic_user_parallel_test_query__402", "medic_user_parallel_test_query__403", "medic_user_parallel_test_query__404", "medic_user_parallel_test_query__405", "medic_user_parallel_test_query__406", "medic_user_parallel_test_query__407", "medic_user_parallel_test_query__408", "medic_user_parallel_test_query__409", "medic_user_parallel_test_query__41", "medic_user_parallel_test_query__410", "medic_user_parallel_test_query__411", "medic_user_parallel_test_query__412", "medic_user_parallel_test_query__413", "medic_user_parallel_test_query__414", "medic_user_parallel_test_query__415", "medic_user_parallel_test_query__416", "medic_user_parallel_test_query__417", "medic_user_parallel_test_query__418", "medic_user_parallel_test_query__419", "medic_user_parallel_test_query__42", "medic_user_parallel_test_query__420", "medic_user_parallel_test_query__421", "medic_user_parallel_test_query__422", "medic_user_parallel_test_query__423", "medic_user_parallel_test_query__424", "medic_user_parallel_test_query__425", "medic_user_parallel_test_query__426", "medic_user_parallel_test_query__427", "medic_user_parallel_test_query__428", "medic_user_parallel_test_query__429", "medic_user_parallel_test_query__43", "medic_user_parallel_test_query__430", "medic_user_parallel_test_query__431", "medic_user_parallel_test_query__432", "medic_user_parallel_test_query__433", "medic_user_parallel_test_query__434", "medic_user_parallel_test_query__435", "medic_user_parallel_test_query__436", "medic_user_parallel_test_query__437", "medic_user_parallel_test_query__438", "medic_user_parallel_test_query__439", "medic_user_parallel_test_query__44", "medic_user_parallel_test_query__440", "medic_user_parallel_test_query__441", "medic_user_parallel_test_query__442", "medic_user_parallel_test_query__443", "medic_user_parallel_test_query__444", "medic_user_parallel_test_query__445", "medic_user_parallel_test_query__446", "medic_user_parallel_test_query__447", "medic_user_parallel_test_query__448", "medic_user_parallel_test_query__449", "medic_user_parallel_test_query__45", "medic_user_parallel_test_query__450", "medic_user_parallel_test_query__451", "medic_user_parallel_test_query__452", "medic_user_parallel_test_query__453", "medic_user_parallel_test_query__454", "medic_user_parallel_test_query__455", "medic_user_parallel_test_query__456", "medic_user_parallel_test_query__457", "medic_user_parallel_test_query__458", "medic_user_parallel_test_query__459", "medic_user_parallel_test_query__46", "medic_user_parallel_test_query__460", "medic_user_parallel_test_query__461", "medic_user_parallel_test_query__462", "medic_user_parallel_test_query__463", "medic_user_parallel_test_query__464", "medic_user_parallel_test_query__465", "medic_user_parallel_test_query__466", "medic_user_parallel_test_query__467", "medic_user_parallel_test_query__468", "medic_user_parallel_test_query__469", "medic_user_parallel_test_query__47", "medic_user_parallel_test_query__470", "medic_user_parallel_test_query__471", "medic_user_parallel_test_query__472", "medic_user_parallel_test_query__473", "medic_user_parallel_test_query__474", "medic_user_parallel_test_query__475", "medic_user_parallel_test_query__476", "medic_user_parallel_test_query__477", "medic_user_parallel_test_query__478", "medic_user_parallel_test_query__479", "medic_user_parallel_test_query__48", "medic_user_parallel_test_query__480", "medic_user_parallel_test_query__481", "medic_user_parallel_test_query__482", "medic_user_parallel_test_query__483", "medic_user_parallel_test_query__484", "medic_user_parallel_test_query__485", "medic_user_parallel_test_query__486", "medic_user_parallel_test_query__487", "medic_user_parallel_test_query__488", "medic_user_parallel_test_query__489", "medic_user_parallel_test_query__49", "medic_user_parallel_test_query__490", "medic_user_parallel_test_query__491", "medic_user_parallel_test_query__492", "medic_user_parallel_test_query__493", "medic_user_parallel_test_query__494", "medic_user_parallel_test_query__495", "medic_user_parallel_test_query__496", "medic_user_parallel_test_query__497", "medic_user_parallel_test_query__498", "medic_user_parallel_test_query__499", "medic_user_parallel_test_query__5", "medic_user_parallel_test_query__50", "medic_user_parallel_test_query__500", "medic_user_parallel_test_query__501", "medic_user_parallel_test_query__502", "medic_user_parallel_test_query__503", "medic_user_parallel_test_query__504", "medic_user_parallel_test_query__505", "medic_user_parallel_test_query__506", "medic_user_parallel_test_query__507", "medic_user_parallel_test_query__508", "medic_user_parallel_test_query__509", "medic_user_parallel_test_query__51", "medic_user_parallel_test_query__510", "medic_user_parallel_test_query__511", "medic_user_parallel_test_query__512", "medic_user_parallel_test_query__513", "medic_user_parallel_test_query__514", "medic_user_parallel_test_query__515", "medic_user_parallel_test_query__516", "medic_user_parallel_test_query__517", "medic_user_parallel_test_query__518", "medic_user_parallel_test_query__519", "medic_user_parallel_test_query__52", "medic_user_parallel_test_query__520", "medic_user_parallel_test_query__521", "medic_user_parallel_test_query__522", "medic_user_parallel_test_query__523", "medic_user_parallel_test_query__524", "medic_user_parallel_test_query__525", "medic_user_parallel_test_query__526", "medic_user_parallel_test_query__527", "medic_user_parallel_test_query__528", "medic_user_parallel_test_query__529", "medic_user_parallel_test_query__53", "medic_user_parallel_test_query__530", "medic_user_parallel_test_query__531", "medic_user_parallel_test_query__532", "medic_user_parallel_test_query__533", "medic_user_parallel_test_query__534", "medic_user_parallel_test_query__535", "medic_user_parallel_test_query__536", "medic_user_parallel_test_query__537", "medic_user_parallel_test_query__538", "medic_user_parallel_test_query__539", "medic_user_parallel_test_query__54", "medic_user_parallel_test_query__540", "medic_user_parallel_test_query__541", "medic_user_parallel_test_query__542", "medic_user_parallel_test_query__543", "medic_user_parallel_test_query__544", "medic_user_parallel_test_query__545", "medic_user_parallel_test_query__546", "medic_user_parallel_test_query__547", "medic_user_parallel_test_query__548", "medic_user_parallel_test_query__549", "medic_user_parallel_test_query__55", "medic_user_parallel_test_query__550", "medic_user_parallel_test_query__551", "medic_user_parallel_test_query__552", "medic_user_parallel_test_query__553", "medic_user_parallel_test_query__554", "medic_user_parallel_test_query__555", "medic_user_parallel_test_query__556", "medic_user_parallel_test_query__557", "medic_user_parallel_test_query__558", "medic_user_parallel_test_query__559", "medic_user_parallel_test_query__56", "medic_user_parallel_test_query__560", "medic_user_parallel_test_query__561", "medic_user_parallel_test_query__562", "medic_user_parallel_test_query__563", "medic_user_parallel_test_query__564", "medic_user_parallel_test_query__565", "medic_user_parallel_test_query__566", "medic_user_parallel_test_query__567", "medic_user_parallel_test_query__568", "medic_user_parallel_test_query__569", "medic_user_parallel_test_query__57", "medic_user_parallel_test_query__570", "medic_user_parallel_test_query__571", "medic_user_parallel_test_query__572", "medic_user_parallel_test_query__573", "medic_user_parallel_test_query__574", "medic_user_parallel_test_query__575", "medic_user_parallel_test_query__576", "medic_user_parallel_test_query__577", "medic_user_parallel_test_query__578", "medic_user_parallel_test_query__579", "medic_user_parallel_test_query__58", "medic_user_parallel_test_query__580", "medic_user_parallel_test_query__581", "medic_user_parallel_test_query__582", "medic_user_parallel_test_query__583", "medic_user_parallel_test_query__584", "medic_user_parallel_test_query__585", "medic_user_parallel_test_query__586", "medic_user_parallel_test_query__587", "medic_user_parallel_test_query__588", "medic_user_parallel_test_query__589", "medic_user_parallel_test_query__59", "medic_user_parallel_test_query__590", "medic_user_parallel_test_query__591", "medic_user_parallel_test_query__592", "medic_user_parallel_test_query__593", "medic_user_parallel_test_query__594", "medic_user_parallel_test_query__595", "medic_user_parallel_test_query__596", "medic_user_parallel_test_query__597", "medic_user_parallel_test_query__598", "medic_user_parallel_test_query__599", "medic_user_parallel_test_query__6", "medic_user_parallel_test_query__60", "medic_user_parallel_test_query__600", "medic_user_parallel_test_query__601", "medic_user_parallel_test_query__602", "medic_user_parallel_test_query__603", "medic_user_parallel_test_query__604", "medic_user_parallel_test_query__605", "medic_user_parallel_test_query__606", "medic_user_parallel_test_query__607", "medic_user_parallel_test_query__608", "medic_user_parallel_test_query__609", "medic_user_parallel_test_query__61", "medic_user_parallel_test_query__610", "medic_user_parallel_test_query__611", "medic_user_parallel_test_query__612", "medic_user_parallel_test_query__613", "medic_user_parallel_test_query__614", "medic_user_parallel_test_query__615", "medic_user_parallel_test_query__616", "medic_user_parallel_test_query__617", "medic_user_parallel_test_query__618", "medic_user_parallel_test_query__619", "medic_user_parallel_test_query__62", "medic_user_parallel_test_query__620", "medic_user_parallel_test_query__621", "medic_user_parallel_test_query__622", "medic_user_parallel_test_query__623", "medic_user_parallel_test_query__624", "medic_user_parallel_test_query__625", "medic_user_parallel_test_query__626", "medic_user_parallel_test_query__627", "medic_user_parallel_test_query__628", "medic_user_parallel_test_query__629", "medic_user_parallel_test_query__63", "medic_user_parallel_test_query__630", "medic_user_parallel_test_query__631", "medic_user_parallel_test_query__632", "medic_user_parallel_test_query__633", "medic_user_parallel_test_query__634", "medic_user_parallel_test_query__635", "medic_user_parallel_test_query__636", "medic_user_parallel_test_query__637", "medic_user_parallel_test_query__638", "medic_user_parallel_test_query__639", "medic_user_parallel_test_query__64", "medic_user_parallel_test_query__640", "medic_user_parallel_test_query__641", "medic_user_parallel_test_query__642", "medic_user_parallel_test_query__643", "medic_user_parallel_test_query__644", "medic_user_parallel_test_query__645", "medic_user_parallel_test_query__646", "medic_user_parallel_test_query__647", "medic_user_parallel_test_query__648", "medic_user_parallel_test_query__649", "medic_user_parallel_test_query__65", "medic_user_parallel_test_query__650", "medic_user_parallel_test_query__651", "medic_user_parallel_test_query__652", "medic_user_parallel_test_query__653", "medic_user_parallel_test_query__654", "medic_user_parallel_test_query__655", "medic_user_parallel_test_query__656", "medic_user_parallel_test_query__657", "medic_user_parallel_test_query__658", "medic_user_parallel_test_query__659", "medic_user_parallel_test_query__66", "medic_user_parallel_test_query__660", "medic_user_parallel_test_query__661", "medic_user_parallel_test_query__662", "medic_user_parallel_test_query__663", "medic_user_parallel_test_query__664", "medic_user_parallel_test_query__665", "medic_user_parallel_test_query__666", "medic_user_parallel_test_query__667", "medic_user_parallel_test_query__668", "medic_user_parallel_test_query__669", "medic_user_parallel_test_query__67", "medic_user_parallel_test_query__670", "medic_user_parallel_test_query__671", "medic_user_parallel_test_query__672", "medic_user_parallel_test_query__673", "medic_user_parallel_test_query__674", "medic_user_parallel_test_query__675", "medic_user_parallel_test_query__676", "medic_user_parallel_test_query__677", "medic_user_parallel_test_query__678", "medic_user_parallel_test_query__679", "medic_user_parallel_test_query__68", "medic_user_parallel_test_query__680", "medic_user_parallel_test_query__681", "medic_user_parallel_test_query__682", "medic_user_parallel_test_query__683", "medic_user_parallel_test_query__684", "medic_user_parallel_test_query__685", "medic_user_parallel_test_query__686", "medic_user_parallel_test_query__687", "medic_user_parallel_test_query__688", "medic_user_parallel_test_query__689", "medic_user_parallel_test_query__69", "medic_user_parallel_test_query__690", "medic_user_parallel_test_query__691", "medic_user_parallel_test_query__692", "medic_user_parallel_test_query__693", "medic_user_parallel_test_query__694", "medic_user_parallel_test_query__695", "medic_user_parallel_test_query__696", "medic_user_parallel_test_query__697", "medic_user_parallel_test_query__698", "medic_user_parallel_test_query__699", "medic_user_parallel_test_query__7", "medic_user_parallel_test_query__70", "medic_user_parallel_test_query__700", "medic_user_parallel_test_query__701", "medic_user_parallel_test_query__702", "medic_user_parallel_test_query__703", "medic_user_parallel_test_query__704", "medic_user_parallel_test_query__705", "medic_user_parallel_test_query__706", "medic_user_parallel_test_query__707", "medic_user_parallel_test_query__708", "medic_user_parallel_test_query__709", "medic_user_parallel_test_query__71", "medic_user_parallel_test_query__710", "medic_user_parallel_test_query__711", "medic_user_parallel_test_query__712", "medic_user_parallel_test_query__713", "medic_user_parallel_test_query__714", "medic_user_parallel_test_query__715", "medic_user_parallel_test_query__716", "medic_user_parallel_test_query__717", "medic_user_parallel_test_query__718", "medic_user_parallel_test_query__719", "medic_user_parallel_test_query__72", "medic_user_parallel_test_query__720", "medic_user_parallel_test_query__721", "medic_user_parallel_test_query__722", "medic_user_parallel_test_query__723", "medic_user_parallel_test_query__724", "medic_user_parallel_test_query__725", "medic_user_parallel_test_query__726", "medic_user_parallel_test_query__727", "medic_user_parallel_test_query__728", "medic_user_parallel_test_query__729", "medic_user_parallel_test_query__73", "medic_user_parallel_test_query__730", "medic_user_parallel_test_query__731", "medic_user_parallel_test_query__732", "medic_user_parallel_test_query__733", "medic_user_parallel_test_query__734", "medic_user_parallel_test_query__735", "medic_user_parallel_test_query__736", "medic_user_parallel_test_query__737", "medic_user_parallel_test_query__738", "medic_user_parallel_test_query__739", "medic_user_parallel_test_query__74", "medic_user_parallel_test_query__740", "medic_user_parallel_test_query__741", "medic_user_parallel_test_query__742", "medic_user_parallel_test_query__743", "medic_user_parallel_test_query__744", "medic_user_parallel_test_query__745", "medic_user_parallel_test_query__746", "medic_user_parallel_test_query__747", "medic_user_parallel_test_query__748", "medic_user_parallel_test_query__749", "medic_user_parallel_test_query__75", "medic_user_parallel_test_query__750", "medic_user_parallel_test_query__76", "medic_user_parallel_test_query__77", "medic_user_parallel_test_query__78", "medic_user_parallel_test_query__79", "medic_user_parallel_test_query__8", "medic_user_parallel_test_query__80", "medic_user_parallel_test_query__81", "medic_user_parallel_test_query__82", "medic_user_parallel_test_query__83", "medic_user_parallel_test_query__84", "medic_user_parallel_test_query__85", "medic_user_parallel_test_query__86", "medic_user_parallel_test_query__87", "medic_user_parallel_test_query__88", "medic_user_parallel_test_query__89", "medic_user_parallel_test_query__9", "medic_user_parallel_test_query__90", "medic_user_parallel_test_query__91", "medic_user_parallel_test_query__92", "medic_user_parallel_test_query__93", "medic_user_parallel_test_query__94", "medic_user_parallel_test_query__95", "medic_user_parallel_test_query__96", "medic_user_parallel_test_query__97", "medic_user_parallel_test_query__98", "medic_user_parallel_test_query__99"};
    final static int[] times = {3565,3632,3711,3716,3769,3773,3782,3785,3823,3826,6475,6486,6487,6488,6492,6494,6528,6546,6547,6576,7757,7806,7817,7817,7832,7852,7879,7880,7923,7943,8549,8556,8562,8605,8659,8681,8683,8711,8723,8726,9265,9276,9276,9282,9309,9321,9323,9345,9381,9405,9892,9907,9971,9979,10029,10050,10053,10074,10076,10093,10969,11024,11028,11035,11048,11112,11121,11122,11129,11158,12044,12056,12069,12073,12087,12106,12139,12161,12166,12534,13057,13071,13087,13101,13104,13109,13139,13177,13222,13236,14790,14799,14799,14865,14881,14922,14926,14927,14942,14971,16026,16076,16092,16103,16105,16127,16151,16173,16173,16322,16803,16858,16861,16882,16887,16900,16935,16939,16985,17009,17226,17239,17291,17293,17301,17317,17368,17412,17421,17543,17555,17603,17608,17621,17629,17632,17665,17673,17677,17700,18808,18830,18838,18847,18908,18917,18974,18999,19001,19161,19848,20085,20096,20139,20173,20265,20265,20265,20279,20281,20849,21001,21004,21028,21034,21059,21123,21132,21140,21141,21926,22102,22105,22166,22257,22292,22345,22351,22356,22465,23210,23219,23231,23334,23334,23337,23338,23343,23352,23465,23715,23853,23855,23877,23881,23974,24002,24063,24138,24146,24369,24372,24468,24469,24474,24499,24646,24650,24670,24819,25828,25832,25836,25982,26073,26077,26116,26121,26157,26250,27041,27047,27088,27121,27146,27193,27202,27246,27288,27414,27440,27477,27500,27516,27547,27792,27802,27812,27832,27841,28575,28745,28803,28804,28872,28940,28992,28993,29004,29008,29199,29310,29334,29410,29415,29438,29474,29475,29572,29590,29640,29722,29807,29860,29871,29893,29895,29896,29910,29911,30275,30276,30410,30436,30437,30670,30704,30708,30711,30846,31413,31693,31708,31838,31841,31841,31847,31848,31998,32003,35106,35248,35267,35302,35304,35415,35468,35473,35500,35544,36523,36661,36661,36684,36690,36766,36768,36771,36941,36953,37874,37874,37965,37994,38131,38147,38152,38153,38247,38255,38424,38429,38554,38564,38578,38586,38598,38601,38672,38994,39385,39522,39548,39557,39559,39559,39625,39678,39724,39820,40156,40169,40199,40205,40233,40261,40273,40299,40305,40308,40313,40319,40330,40345,40347,40350,40353,40376,40378,40379,40444,40449,40461,40481,40493,40513,40531,40597,40613,40622,40638,40689,40710,40738,40759,40770,40798,40814,40817,40820,40888,40953,40990,41009,41059,41061,41099,41124,41188,41192,42372,42616,42738,42740,42788,42788,42792,42794,42806,42899,44143,44159,44291,44402,44440,44443,44541,44542,44562,44583,45418,45446,45457,45521,45611,45724,45725,45728,45731,45746,46101,46102,46150,46242,46246,46260,46262,46270,46532,46654,46778,46792,46794,46803,46924,46955,46960,47153,47153,47217,47431,47591,47604,47609,47609,47609,47681,47854,47868,47959,47969,47971,47997,48022,48034,48145,48146,48152,48409,48490,48871,48874,49008,49019,49039,49040,49044,49051,49147,49172,49469,49480,49617,49630,49644,49656,49800,49819,49821,49829,49837,49913,49990,50011,50012,50016,50035,50074,50362,50410,51403,51403,51404,51543,51580,51589,51589,51663,51946,52015};

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    /* AWS */
    private static final String BUCKET_NAME = "medical-records-pgc";
    private static final Regions clientRegion = Regions.SA_EAST_1;
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(clientRegion)
            .build();

    /* AWS */
    //Todo: Adicionar politicas dos usuarios no ledger
    //Todo: Trazer todos os registros por usuario
    private static void handleUserChoice(Contract contract) {
        try {
            int choice;
            String input;
            Scanner numReader = new Scanner(System.in);
            Scanner txtReader = new Scanner(System.in);

            do {
                //Todo: salvar localmente
                System.out.println(" 1 - Criar GlobalParameters");
                System.out.println(" 2 - Criar Authority");
                System.out.println(" 3 - Criar User");
                System.out.println(" 4 - Enviar MedRecord");
                System.out.println(" 5 - Query GlobalParameters");
                System.out.println(" 6 - Query Authority");
                System.out.println(" 7 - Query User");
                System.out.println(" 8 - Query MedRecord");
                System.out.println(" 9 - Query MedRecordFile");
                System.out.println("10 - Adicionar atributo a Authority");
                //System.out.println("10 - Gerar chave secreta para usuário");
                System.out.println("11 - Todos arquivos do usuário");
                System.out.println("12 - Criação de usuários e envio de pdf em paralelo");
                System.out.println("13 - Obtenção dos pdf em paralelo");
                System.out.println("14 - Teste de velocidade - Encriptação");
                System.out.println("15 - Teste de velocidade - Decriptação");
                System.out.println("0 - Sair");
                choice = numReader.nextInt();

                switch (choice) {
                    case 1:
                        System.out.println("Digite a chave do GP");
                        String gpCreatekey = txtReader.nextLine();
                        createGP(contract, gpCreatekey);
                        System.out.println("Sucesso");
                        break;
                    case 2:
                        System.out.println("Digite a chave da Authority");
                        String authKeyAuthCreate = txtReader.nextLine();
                        System.out.println("Digite o ID da autoridade");
                        String authIDAuthCreate = txtReader.nextLine();
                        System.out.println("Digite a chave do gp a ser utilizado na criação");
                        String gpKeyAuthCreate = txtReader.nextLine();
                        GlobalParameters gpAuthCreate = getGP(contract, gpKeyAuthCreate);
                        System.out.println("Digite o atributo inicial da autoridade");
                        String attrAuthCreate = txtReader.nextLine();
                        AuthorityKeys akAuthCreate =
                                createAuthority(contract, authKeyAuthCreate, authIDAuthCreate, gpAuthCreate, attrAuthCreate);
                        Util.saveSecretKeys(akAuthCreate);
                        System.out.println("Sucesso");
                        break;
                    case 3:
                        System.out.println("Digite o id do usuário");
                        String userIDUserCreate = txtReader.nextLine();
                        System.out.println("Digite os atributos do usuário (separados por espaço)");
                        String userAttributesUserCreate = txtReader.nextLine();
                        createUser(contract, userIDUserCreate, userIDUserCreate, userAttributesUserCreate.split(" "));
                        System.out.println("Success");
                        break;
                    case 4:
                        System.out.println("Digite o caminho do arquivo");
                        String filePathMedRecordCreate = txtReader.nextLine();
                        byte[] clearFile = FileUtils.readFileToByteArray(new File(filePathMedRecordCreate));
                        System.out.println("Digite o id do usuário que está enviando o arquivo");
                        String userIDMedRecordCreate = txtReader.nextLine();
                        User userMedRecordCreate = getUser(contract, userIDMedRecordCreate);
                        System.out.println("Digite o id do GlobalParameter");
                        String gpIDMedRecordCreate = txtReader.nextLine();
                        GlobalParameters gp = getGP(contract, gpIDMedRecordCreate);
                        System.out.println("Digite a policy (notação infixa)");
                        String policyMedRecordCreate = txtReader.nextLine();
                        System.out.println("Digite o id da autoridade");
                        String authId = txtReader.nextLine();
                        Authority auth = getAuthority(contract, authId);

                        MedRecord md = createMedRecord(contract, clearFile, userMedRecordCreate, policyMedRecordCreate, gp, auth, gpIDMedRecordCreate);
                        System.out.println("Nome do arquivo no S3: " + md.getFileName());
                        break;
                    case 5:
                        System.out.println("Digite o id do GlobalParameter");
                        String gpIDQueryGP = txtReader.nextLine();
                        String gpJson = Util.objToJson(getGP(contract, gpIDQueryGP));
                        System.out.println(gpJson);
                        break;
                    case 6:
                        System.out.println("Digite o ID da Authority");
                        String authIdQueryAuth = txtReader.nextLine();
                        String authJson = Util.objToJson(getAuthority(contract, authIdQueryAuth));
                        System.out.println(authJson);
                        break;
                    case 7:
                        System.out.println("Digite o ID do usuário");
                        input = txtReader.nextLine();
                        String userJson = Util.objToJson(getUser(contract, input));
                        System.out.println(userJson);
                        break;
                    case 8:
                        System.out.println("Digite o ID do MedRecord");
                        input = txtReader.nextLine();
                        String medRecordJson = Util.objToJson(getMedRecord(contract, input));
                        System.out.println(medRecordJson);
                        break;
                    case 9:
                        System.out.println("Digite o ID do MedRecord");
                        String medRecordID = txtReader.nextLine();
                        System.out.println("Digite o ID do usuário");
                        String userId = txtReader.nextLine();
                        System.out.println("Digite o ID do gp");
                        String gpId = txtReader.nextLine();
                        System.out.println("Digite o caminho do arquivo decriptado");
                        String filePath = txtReader.nextLine();
                        String medRecFileJson = Util.objToJson(getMedRecordFile(contract, medRecordID, gpId, userId, filePath, 1));
                        System.out.println(medRecFileJson);
                        break;
                    case 10:
                        System.out.println("Digite o ID da Authority");
                        String authID = txtReader.nextLine();
                        System.out.println("Digite o ID do GP");
                        String gpId10 = txtReader.nextLine();
                        System.out.println("Digite o novo atributo");
                        String att10 = txtReader.nextLine();
                        String pkJson = Util.objToJson(addAttribute(contract, authID, gpId10, att10));
                        System.out.println(pkJson);
                        break;
                    //Todo: gerar e salvar chaves secretas do usuário
//                    case 11:
//                        System.out.println("Digite o ID do usuário");
//                        input = txtReader.nextLine();
//                        User user = getUser(contract, input);
//                        //Todo: verificar possibilidade de múltiplas
//                        System.out.println("Digite o id da autoridade");
//                        input = txtReader.nextLine();
//                        Authority auth = getAuthority(contract, input);
//                        System.out.println("Digite atributos do usuário (separados por espaço)");
//                        input = txtReader.nextLine();
//                        Util.generateUserSecretKeys(user.getId(), auth, input.split(" "));
                    case 11:
                        getAllUserMedRecords("USER1/").forEach(System.out::println);
                        break;
                    case 12:
                        System.out.println("Digite o número de instâncias");
                        int instances12 = numReader.nextInt();
                        automatedUserAndMedRecord(contract, instances12);
                        break;
                    case 13:
                        System.out.println("Digite o número de instâncias");
                        int instances13 = numReader.nextInt();
                        automatedQueryMedRecord(contract, instances13);
                        break;
                    case 14:
                        GlobalParameters gpEnc = getGP(contract, "chave");
                        testEncript(gpEnc);
                        break;
                    case 15:
                        GlobalParameters gpDec = getGP(contract, "chave");
                        testDecript(gpDec);
                        break;
                    case 16:
                        GlobalParameters gpSup = getGP(contract, "chave");
                        String[] atts = {"ATTRIBUTE0","ATTRIBUTE1","ATTRIBUTE2","ATTRIBUTE3","ATTRIBUTE4","ATTRIBUTE5","ATTRIBUTE6","ATTRIBUTE7","ATTRIBUTE8","ATTRIBUTE9","ATTRIBUTE10","ATTRIBUTE11","ATTRIBUTE12","ATTRIBUTE13","ATTRIBUTE14","ATTRIBUTE15","ATTRIBUTE16","ATTRIBUTE17","ATTRIBUTE18","ATTRIBUTE19","ATTRIBUTE20","ATTRIBUTE21","ATTRIBUTE22","ATTRIBUTE23","ATTRIBUTE24","ATTRIBUTE25","ATTRIBUTE26","ATTRIBUTE27","ATTRIBUTE28","ATTRIBUTE29","ATTRIBUTE30","ATTRIBUTE31","ATTRIBUTE32","ATTRIBUTE33","ATTRIBUTE34","ATTRIBUTE35","ATTRIBUTE36","ATTRIBUTE37","ATTRIBUTE38","ATTRIBUTE39","ATTRIBUTE40","ATTRIBUTE41","ATTRIBUTE42","ATTRIBUTE43","ATTRIBUTE44","ATTRIBUTE45","ATTRIBUTE46","ATTRIBUTE47","ATTRIBUTE48","ATTRIBUTE49","ATTRIBUTE50","ATTRIBUTE51","ATTRIBUTE52","ATTRIBUTE53","ATTRIBUTE54","ATTRIBUTE55","ATTRIBUTE56","ATTRIBUTE57","ATTRIBUTE58","ATTRIBUTE59","ATTRIBUTE60","ATTRIBUTE61","ATTRIBUTE62","ATTRIBUTE63","ATTRIBUTE64","ATTRIBUTE65","ATTRIBUTE66","ATTRIBUTE67","ATTRIBUTE68","ATTRIBUTE69","ATTRIBUTE70","ATTRIBUTE71","ATTRIBUTE72","ATTRIBUTE73","ATTRIBUTE74","ATTRIBUTE75","ATTRIBUTE76","ATTRIBUTE77","ATTRIBUTE78","ATTRIBUTE79","ATTRIBUTE80","ATTRIBUTE81","ATTRIBUTE82","ATTRIBUTE83","ATTRIBUTE84","ATTRIBUTE85","ATTRIBUTE86","ATTRIBUTE87","ATTRIBUTE88","ATTRIBUTE89","ATTRIBUTE90","ATTRIBUTE91","ATTRIBUTE92","ATTRIBUTE93","ATTRIBUTE94","ATTRIBUTE95","ATTRIBUTE96","ATTRIBUTE97","ATTRIBUTE98","ATTRIBUTE99"};
                        AuthorityKeys ak = DCPABE.authoritySetup("SUPER_AUTH", gpSup, atts);
                        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

                        Authority aut = new Authority("SUPER_AUTH");
                        for(String att : atts) {
                            String eg1ai = new String(encoder.encode(ak.getPublicKeys().get(att).getEg1g1ai()));
                            String g1yi = new String(encoder.encode(ak.getPublicKeys().get(att).getG1yi()));
                            aut.addPublicKey(att, eg1ai, g1yi);
                        }

                        Util.writeObjToJSON("SUPER_AUTH_pks.json", aut);
                        Util.writeObjToJSON("SUPER_AUTH.json", ak);
                        Util.saveSecretKeys(ak);
                        break;
                }

            } while (choice != 0);
        } catch (IOException | ContractException | TimeoutException | InterruptedException | NoSuchAlgorithmException |
                InvalidCipherTextException | ClassNotFoundException e) {
            System.out.println("Erro: ");
            System.out.println(e.getMessage());
        }
    }

    private static void automatedUserAndMedRecord(Contract contract, int repetitions) {
        int count = 0;
        PrintStream old = null;
        try {
            File file = new File("output.txt");
            PrintStream stream = new PrintStream(file);
            old = System.out;
            System.setOut(stream);
        } catch (Exception e) {
            System.out.println("Erro no redirect do print");
        }

        Integer[] sequence = IntStream.rangeClosed(1, repetitions).boxed().toArray(Integer[]::new);
        Stream parallelStream = Arrays.asList(sequence).parallelStream();
        ForkJoinPool fkp = null;
        Instant start = Instant.now();
        try {
            fkp = new ForkJoinPool(repetitions);
            fkp.submit(() ->
                    parallelStream.forEach((id) -> {
                        try {
                            //3
                            // String userIDUserCreate = "user_parallel_teste_" + Util.getRandomString(7) + "-" + id;
                            String userIDUserCreate = "medic_user_parallel_test_query__10b_" + id;

                            String userAttributesUserCreate = "MEDIC";
                            createUser(contract, userIDUserCreate, userIDUserCreate, userAttributesUserCreate.split(" "));

                            //4
                            String filePathMedRecordCreate = "DCPABE.pdf";
                            byte[] clearFile = FileUtils.readFileToByteArray(new File(filePathMedRecordCreate));
                            String userIDMedRecordCreate = userIDUserCreate;
                            User userMedRecordCreate = getUser(contract, userIDMedRecordCreate);
                            String gpIDMedRecordCreate = "chave";
                            GlobalParameters gp = getGP(contract, gpIDMedRecordCreate);

                            String policyMedRecordCreate = "MEDIC"; // TODO: Authority does not have the id attribute
                            String authId = "a1";
                            Authority auth = getAuthority(contract, authId);

                            MedRecord md = createMedRecord(contract, clearFile, userMedRecordCreate, policyMedRecordCreate, gp, auth, gpIDMedRecordCreate);
                            //System.out.println("Finished for id " + id);
                        } catch (IOException | ContractException | TimeoutException | InterruptedException | NoSuchAlgorithmException |
                                InvalidCipherTextException e) {
                            System.out.println("Erro: ");
                            System.out.println(e.getMessage());
                        }
                    })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Erro:" + e.getMessage());
        } finally {
            if (fkp != null) {
                fkp.shutdown();
            }
        }
        Instant end = Instant.now();
        long total = Duration.between(start, end).toMillis();
        //System.out.println("Total: " + total);
        //System.out.println("Execution complete");

        try {
            System.out.flush();
            System.setOut(old);
        } catch (Exception e) {
            System.out.println("Erro no re-redirect do print");
        }
    }

    private static void automatedQueryMedRecord(Contract contract, int repetitions) {
        int count = 0;
        PrintStream old = null;
        try {
            File file = new File("output.txt");
            PrintStream stream = new PrintStream(file);
            old = System.out;
            System.setOut(stream);
        } catch (Exception e) {
            System.out.println("Erro no redirect do print");
        }

        Integer[] sequence = IntStream.rangeClosed(1, repetitions).boxed().toArray(Integer[]::new);
        Stream parallelStream = Arrays.asList(sequence).parallelStream();
        ForkJoinPool fkp = null;
        Instant start = Instant.now();
        try {
            fkp = new ForkJoinPool(repetitions);
            fkp.submit(() ->
                            parallelStream.forEach((id) -> {
                                try {
                                    //3
//                                  query MedRecord
//                                  query user
//                                  query gp
//                                  baixa arquivo
//                                  decripta chave
//                                  decripta arquivo
                                    int numericId = ((Integer) id).intValue();

                                    String userIDUserCreate = "medic_user_size_test_batch_6_id_" + id;

                                    String userAttributesUserCreate = "MEDIC CARDIOLOGIST SURGEON";
                                    try {
                                        createMedRecord(contract, userIDUserCreate, "teste", "teste", "teste", "auth", "gp");
                                    } catch (TimeoutException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    //getMedRecordFile(contract, medRecordIds[/*numericId - */1], "chave", userIds[/*numericId - */1], "clearfile-storage/" + numericId + ".pdf", numericId);
                                    // System.out.println("Finished for id " + numericId);
                                } catch (/*IOException | */ContractException/* | InvalidCipherTextException | ClassNotFoundException*/ e) {
                                    System.out.println("Erro: ");
                                    System.out.println(e.getMessage());
                                }
                            })
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Erro:" + e.getMessage());
        } finally {
            if (fkp != null) {
                fkp.shutdown();
            }
        }
        Instant end = Instant.now();
        long total = Duration.between(start, end).toMillis();
        System.out.println("Total\t" + total);
        // System.out.println("Execution complete");

        try {
            System.out.flush();
            System.setOut(old);
        } catch (Exception e) {
            System.out.println("Erro no re-redirect do print");
        }
    }

    private static void testEncript(GlobalParameters gp) {
        PrintStream old = null;
        try {
            File file = new File("output.txt");
            PrintStream stream = new PrintStream(file);
            old = System.out;
            System.setOut(stream);
        } catch (Exception e) {
            System.out.println("Erro no redirect do print");
        }

        String path = "sample-pdfs/";
        String[] files = {"300kb", "300kb", "500kb", "1mb", "5mb", "10mb", "30mb", "60mb"};
        //String[] files = {"1mb"};

        for(String file : files) {
            File f = new File(path + file + ".pdf");
            System.out.println(file);
            try {
                int size = 1;
                byte[] fileBytes = FileUtils.readFileToByteArray(f);
                String[] atts = {"ATTRIBUTE0","ATTRIBUTE1","ATTRIBUTE2","ATTRIBUTE3","ATTRIBUTE4","ATTRIBUTE5","ATTRIBUTE6","ATTRIBUTE7","ATTRIBUTE8","ATTRIBUTE9","ATTRIBUTE10","ATTRIBUTE11","ATTRIBUTE12","ATTRIBUTE13","ATTRIBUTE14","ATTRIBUTE15","ATTRIBUTE16","ATTRIBUTE17","ATTRIBUTE18","ATTRIBUTE19","ATTRIBUTE20","ATTRIBUTE21","ATTRIBUTE22","ATTRIBUTE23","ATTRIBUTE24","ATTRIBUTE25","ATTRIBUTE26","ATTRIBUTE27","ATTRIBUTE28","ATTRIBUTE29","ATTRIBUTE30","ATTRIBUTE31","ATTRIBUTE32","ATTRIBUTE33","ATTRIBUTE34","ATTRIBUTE35","ATTRIBUTE36","ATTRIBUTE37","ATTRIBUTE38","ATTRIBUTE39","ATTRIBUTE40","ATTRIBUTE41","ATTRIBUTE42","ATTRIBUTE43","ATTRIBUTE44","ATTRIBUTE45","ATTRIBUTE46","ATTRIBUTE47","ATTRIBUTE48","ATTRIBUTE49","ATTRIBUTE50","ATTRIBUTE51","ATTRIBUTE52","ATTRIBUTE53","ATTRIBUTE54","ATTRIBUTE55","ATTRIBUTE56","ATTRIBUTE57","ATTRIBUTE58","ATTRIBUTE59","ATTRIBUTE60","ATTRIBUTE61","ATTRIBUTE62","ATTRIBUTE63","ATTRIBUTE64","ATTRIBUTE65","ATTRIBUTE66","ATTRIBUTE67","ATTRIBUTE68","ATTRIBUTE69","ATTRIBUTE70","ATTRIBUTE71","ATTRIBUTE72","ATTRIBUTE73","ATTRIBUTE74","ATTRIBUTE75","ATTRIBUTE76","ATTRIBUTE77","ATTRIBUTE78","ATTRIBUTE79","ATTRIBUTE80","ATTRIBUTE81","ATTRIBUTE82","ATTRIBUTE83","ATTRIBUTE84","ATTRIBUTE85","ATTRIBUTE86","ATTRIBUTE87","ATTRIBUTE88","ATTRIBUTE89","ATTRIBUTE90","ATTRIBUTE91","ATTRIBUTE92","ATTRIBUTE93","ATTRIBUTE94","ATTRIBUTE95","ATTRIBUTE96","ATTRIBUTE97","ATTRIBUTE98","ATTRIBUTE99"};
                System.out.println(size);
                String[] andArray = new String[size-1];
                Arrays.fill(andArray, "and");
                String[] subAtts = Arrays.copyOfRange(atts, 0, size);
                String[] policyArray = Stream.concat(Arrays.stream(andArray), Arrays.stream(subAtts)).toArray(String[]::new);
                Authority aut = Util.readObjFromJSON("SUPER_AUTH_pks.json", Authority.class);
                byte[] cipheredFile = ABEUtil.encryptTest(fileBytes, String.join(" ", policyArray), gp, aut, 1000);
                try (FileOutputStream fos = new FileOutputStream("encPdfs/"+file+".pdf")) {
                    fos.write(cipheredFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidCipherTextException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.flush();
            System.setOut(old);
        } catch (Exception e) {
            System.out.println("Erro no re-redirect do print");
        }
    }

    private static void testDecript(GlobalParameters gp) {
        PrintStream old = null;
        try {
            File file = new File("output.txt");
            PrintStream stream = new PrintStream(file);
            old = System.out;
            System.setOut(stream);
        } catch (Exception e) {
            System.out.println("Erro no redirect do print");
        }

        String path = "encPdfs/";
        // String[] files = {"300kb", "500kb", "1mb", "5mb", "10mb", "30mb", "60mb"};
        String[] files = {"300kb"};

        for(String file : files) {
            File f = new File(path + file + ".pdf");
            System.out.println(file);
            try {
                int size = 1;
                byte[] fileBytes = FileUtils.readFileToByteArray(f);
                String[] atts = {"ATTRIBUTE0","ATTRIBUTE1","ATTRIBUTE2","ATTRIBUTE3","ATTRIBUTE4","ATTRIBUTE5","ATTRIBUTE6","ATTRIBUTE7","ATTRIBUTE8","ATTRIBUTE9","ATTRIBUTE10","ATTRIBUTE11","ATTRIBUTE12","ATTRIBUTE13","ATTRIBUTE14","ATTRIBUTE15","ATTRIBUTE16","ATTRIBUTE17","ATTRIBUTE18","ATTRIBUTE19","ATTRIBUTE20","ATTRIBUTE21","ATTRIBUTE22","ATTRIBUTE23","ATTRIBUTE24","ATTRIBUTE25","ATTRIBUTE26","ATTRIBUTE27","ATTRIBUTE28","ATTRIBUTE29","ATTRIBUTE30","ATTRIBUTE31","ATTRIBUTE32","ATTRIBUTE33","ATTRIBUTE34","ATTRIBUTE35","ATTRIBUTE36","ATTRIBUTE37","ATTRIBUTE38","ATTRIBUTE39","ATTRIBUTE40","ATTRIBUTE41","ATTRIBUTE42","ATTRIBUTE43","ATTRIBUTE44","ATTRIBUTE45","ATTRIBUTE46","ATTRIBUTE47","ATTRIBUTE48","ATTRIBUTE49","ATTRIBUTE50","ATTRIBUTE51","ATTRIBUTE52","ATTRIBUTE53","ATTRIBUTE54","ATTRIBUTE55","ATTRIBUTE56","ATTRIBUTE57","ATTRIBUTE58","ATTRIBUTE59","ATTRIBUTE60","ATTRIBUTE61","ATTRIBUTE62","ATTRIBUTE63","ATTRIBUTE64","ATTRIBUTE65","ATTRIBUTE66","ATTRIBUTE67","ATTRIBUTE68","ATTRIBUTE69","ATTRIBUTE70","ATTRIBUTE71","ATTRIBUTE72","ATTRIBUTE73","ATTRIBUTE74","ATTRIBUTE75","ATTRIBUTE76","ATTRIBUTE77","ATTRIBUTE78","ATTRIBUTE79","ATTRIBUTE80","ATTRIBUTE81","ATTRIBUTE82","ATTRIBUTE83","ATTRIBUTE84","ATTRIBUTE85","ATTRIBUTE86","ATTRIBUTE87","ATTRIBUTE88","ATTRIBUTE89","ATTRIBUTE90","ATTRIBUTE91","ATTRIBUTE92","ATTRIBUTE93","ATTRIBUTE94","ATTRIBUTE95","ATTRIBUTE96","ATTRIBUTE97","ATTRIBUTE98","ATTRIBUTE99"};
                System.out.println(size);
                String[] andArray = new String[size-1];
                String[] subAtts = Arrays.copyOfRange(atts, 0, size);

                PersonalKeys perKeys = new PersonalKeys("SUPER_USER");

                for (String attribute : subAtts) {
                    SecretKey sk = Util.readAuthoritySecretKey("SUPER_AUTH", attribute);
                    // Util.writeObjToJSON("sk.json", sk);
                    perKeys.addKey(DCPABE.keyGen("SUPER_USER", attribute, sk, gp));
                }

                byte[] clearFile = ABEUtil.decryptTest(fileBytes, perKeys, gp, 100, subAtts);
                // byte[] clearFile = ABEUtil.decrypt(fileBytes, perKeys, gp, subAtts);

                try (FileOutputStream fos = new FileOutputStream("clearfile-storage/"+file+".pdf")) {
                    fos.write(clearFile);
                }
            } catch (IOException | InvalidCipherTextException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.flush();
            System.setOut(old);
        } catch (Exception e) {
            System.out.println("Erro no re-redirect do print");
        }
    }

    public static void main(String[] args) {
        Gateway gateway = null;
        try {
            // Load a file system based wallet for managing identities.
            Path walletPath = Paths.get("wallet");
            Wallet wallet = Wallet.createFileSystemWallet(walletPath);

            // load a CCP
            Path networkConfigPath = Paths.get("..", "..", "first-network", "connection-org1.yaml");

            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(wallet, "user1").networkConfig(networkConfigPath).discovery(true);
            gateway = builder.connect();
            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            handleUserChoice(contract);
        } catch (IOException e) {
            System.out.println("Erro: ");
            System.out.println(e.getMessage());
        } finally {
            assert gateway != null;
            gateway.close();
        }
    }

    public static void main1(String[] args) throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallet.createFileSystemWallet(walletPath);

        // load a CCP
        Path networkConfigPath = Paths.get("..", "..", "first-network", "connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "user1").networkConfig(networkConfigPath).discovery(true);


        final String GP_KEY = "GP014";
        final String AUTH_KEY = "AUT014";
        final String AUTHORITY_ID = "HOSPITAL Y";
        final String ATTRIBUTE1 = "MEDIC";
        final String ATTRIBUTE2 = "CARDIOLOGIST";
        final String USER_KEY1 = "USR0040";
        final String USER_ID1 = UUID.randomUUID().toString();
        final String USER1_ATTRIBUTE1 = "PATIENT";
        final String USER_KEY2 = "USR0041";
        final String USER_ID2 = UUID.randomUUID().toString();
        final String USER2_ATTRIBUTE1 = "MEDIC";
        final String USER2_ATTRIBUTE2 = "CARDIOLOGIST";
        final String USER_KEY3 = "USR0042";
        final String USER_ID3 = UUID.randomUUID().toString();
        final String USER3_ATTRIBUTE1 = "MEDIC";

        final String REC_KEY1 = "REC017";

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            System.out.println("Criando GlobalParameters...");
            /* Criar GlobalParameter */
            GlobalParameters gpOriginal = createGP(contract, GP_KEY);
            System.out.println();

            /* Recupera GlobalParameter */
            System.out.println("Recuperando GlobalParameters salvo no blockchain...");
            GlobalParameters gp = getGP(contract, GP_KEY);
            System.out.println("gp - " + gp);
            System.out.println();

            /* Cria nova autoridade */
            System.out.println("Criando autoridade...");
            AuthorityKeys ak = createAuthority(contract, AUTH_KEY, AUTHORITY_ID, gp, ATTRIBUTE1);
            System.out.println();

            /* Salva ak para manter as chaves secretas */
            System.out.println("Salvando chaves secretas localmente...");
            //Util.saveSecretKeys(ak);
            System.out.println();

            /* Recupera autoridade */
            Authority authority = getAuthority(contract, AUTH_KEY);
            System.out.println(authority.getAuthorityID());
            authority.getPublicKeys().forEach((att, pk) -> {
                System.out.println("att: " + att + "- pk: " + pk);
            });

            /* Cria novo ak para inserir novo atributo */
            System.out.println("Criando outra autoridade para gerar novo atributo...");
            AuthorityKeys akAux = addAttribute(contract, AUTH_KEY, authority, gp, ATTRIBUTE2);
            System.out.println();
            Authority authorityWithNewAtt = getAuthority(contract, AUTH_KEY);
            System.out.println(authorityWithNewAtt.getAuthorityID());
            authorityWithNewAtt.getPublicKeys().forEach((att, pk) -> {
                System.out.println("att: " + att + "- pk: " + pk);
            });

            /* Salva ak para manter as chaves secretas */
            System.out.println("Salvando novas chaves secretas...");
            Util.saveSecretKeys(akAux);
            System.out.println();

            System.out.println("Criando usuários...");
            createUser(contract, USER_KEY1, USER_ID1, USER1_ATTRIBUTE1);
            createUser(contract, USER_KEY2, USER_ID2, USER2_ATTRIBUTE1, USER2_ATTRIBUTE2);
            createUser(contract, USER_KEY3, USER_ID3, USER3_ATTRIBUTE1);

            User user1 = getUser(contract, USER_KEY1);
            System.out.println(user1.getId() + " " + String.join(", ", user1.getAttributes()));
            User user2 = getUser(contract, USER_KEY2);
            System.out.println(user2.getId() + " " + String.join(", ", user2.getAttributes()));
            User user3 = getUser(contract, USER_KEY3);
            System.out.println(user3.getId() + " " + String.join(", ", user3.getAttributes()));
            System.out.println();

            System.out.println("Encriptando PDF...");
            byte[] clearPdf = FileUtils.readFileToByteArray(new File("DCPABE.pdf"));
            byte[] cipheredPdf = ABEUtil.encrypt(clearPdf, "and " + USER2_ATTRIBUTE1 + " " + USER2_ATTRIBUTE2, gp, authorityWithNewAtt);
            System.out.println();

            System.out.println("Calculando hash do arquivo criptografado...");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cipheredPdf);
            String encodedHash = Util.encodeBytesToBase64(hash);
            System.out.println();

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String fileName = Long.toString(timestamp.getTime());
            //Todo: verificar necessidade/possibilidade de transação
            //Todo: mudar nome do campo bucket para fileName
            System.out.println("Enviando Informações do arquivo para o chaincode...");
            createMedRecord(contract, REC_KEY1, USER_ID1, fileName, encodedHash, AUTH_KEY, GP_KEY);
            System.out.println();

            System.out.println("Enviando arquivo para o S3...");
            sendRecordToS3(cipheredPdf, user1.getId(), fileName);
            System.out.println();

            System.out.println("Obtendo dados do arquivo no blockchain...");
            MedRecord medRecord = getMedRecord(contract, REC_KEY1);
            System.out.println(medRecord.getUserId() + " - " + medRecord.getHash() + " - " + medRecord.getFileName());
            System.out.println();

            System.out.println("Baixando arquivo do S3...");
            byte[] cypheredPdf = getRecordFromS3(user1.getId(), medRecord.getFileName());
            System.out.println();

            //Todo: chave secreta do usuário deveria ser salva
            System.out.println("Gerando chave secreta do usuário e decriptando arquivo...");
            PersonalKeys perKeys = new PersonalKeys(user2.getId());

            for (String attribute : user2.getAttributes()) {
                SecretKey sk = Util.readAuthoritySecretKey(authority.getAuthorityID(), attribute);
                Util.writeObjToJSON("sk.json", sk);
                perKeys.addKey(DCPABE.keyGen(user2.getId(), attribute, sk, gp));
            }
            System.out.println();

            byte[] decryptedPdf = ABEUtil.decrypt(cypheredPdf, perKeys, gp, user2.getAttributes().toArray(new String[]{}));

            FileUtils.writeByteArrayToFile(new File("decPdf.pdf"), decryptedPdf);
        }
    }

    private static AuthorityKeys createAuthority(Contract contract, String authKey, String authorityID, GlobalParameters gp, String attribute)
            throws ContractException, TimeoutException, InterruptedException {
        Instant startCreateAuth = Instant.now();
        AuthorityKeys ak = DCPABE.authoritySetup(authorityID, gp, attribute);

        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

        String eg1ai = new String(encoder.encode(ak.getPublicKeys().get(attribute).getEg1g1ai()));
        String g1yi = new String(encoder.encode(ak.getPublicKeys().get(attribute).getG1yi()));
        Util.saveSecretKeys(ak);

        Instant endCreateAuth = Instant.now();
        long totalCreateAuth = Duration.between(startCreateAuth, endCreateAuth).toMillis();
        //System.out.println("Create authority\t" + totalCreateAuth);

        Instant startSendAuth = Instant.now();

        contract.submitTransaction("createAuthority", authKey, authorityID, attribute, eg1ai, g1yi);

        Instant endSendAuth = Instant.now();
        long totalSendAuth = Duration.between(startSendAuth, endSendAuth).toMillis();
        //System.out.println("Send authority\t" + totalSendAuth);
        return ak;
    }

    private static GlobalParameters createGP(Contract contract, String key)
            throws IOException, ContractException, TimeoutException, InterruptedException {
        Instant startCreateGP = Instant.now();
        GlobalParameters gp = DCPABE.globalSetup(160);
        String pairingParameter = new String(Base64.encodeBase64(Util.objToByteArray(gp.getPairingParameters())));
        String g1 = new String(Base64.encodeBase64(gp.getG1().toBytes()));
        Instant endCreateGP = Instant.now();
        long totalCreateGP = Duration.between(startCreateGP, endCreateGP).toMillis();
        //System.out.println("Create gp\t" + totalCreateGP);

        Instant startSendGP = Instant.now();
        contract.submitTransaction("createGlobalParameter", key, pairingParameter, g1);
        Instant endSendGP = Instant.now();
        long totalSendGP = Duration.between(startSendGP, endSendGP).toMillis();
        //System.out.println("Send GP\t" + totalSendGP);
        return gp;
    }

    private static User createUser(Contract contract, String key, String userID, String... attributes)
            throws ContractException, TimeoutException, InterruptedException {
        Instant startCreateUser = Instant.now();
        User user = new User(userID, attributes);
        Instant endCreateUser = Instant.now();
        long totalCreateUser = Duration.between(startCreateUser, endCreateUser).toMillis();
        //System.out.println("Create user: " + totalCreateUser);

//        PersonalKeys perKeys = new PersonalKeys(user.getId());
//
//        for (String attribute : user.getAttributes()) {
//            SecretKey sk = Util.readAuthoritySecretKey("a", attribute);
//            perKeys.addKey(DCPABE.keyGen(user.getId(), attribute, sk, gp));
//        }

        Instant startSendUser = Instant.now();
        contract.submitTransaction("createUser", key, userID, String.join(",", attributes));
        Instant endSendUser = Instant.now();
        long totalSendUser = Duration.between(startSendUser, endSendUser).toMillis();
        //System.out.println("Send user: " + totalSendUser);
        //System.out.println("user    " + userID);
        return user;
    }

    @DoNotCall
    private static MedRecord createMedRecord(Contract contract, String key, String userID, String bucket, String hash, String authID, String gpKey)
            throws ContractException, TimeoutException, InterruptedException {
        MedRecord medRecord = new MedRecord(userID, bucket, hash, authID, gpKey);
        contract.submitTransaction("createMedRecord", key, userID, bucket, hash, String.join(",", authID), gpKey);
        return medRecord;
    }

    private static PublicKey addAttribute(Contract contract, String authKey, String gpId, String attribute)
            throws ContractException, TimeoutException, InterruptedException {
        Authority authority = getAuthority(contract, authKey);
        GlobalParameters gp = getGP(contract, gpId);
        AuthorityKeys ak = DCPABE.authoritySetup(authority.getAuthorityID(), gp, attribute);

        PublicKey pk = ak.getPublicKeys().get(attribute);

        Util.saveSecretKeys(ak);

        contract.submitTransaction("addPublicKey", authKey, attribute, Util.getBase64Eg1g1ai(pk.getEg1g1ai()),
                Util.getBase64G1yi(pk.getG1yi()));

        return pk;

    }

    private static AuthorityKeys addAttribute(Contract contract, String authKey, Authority authority, GlobalParameters gp, String attribute)
            throws ContractException, TimeoutException, InterruptedException {
        AuthorityKeys ak = DCPABE.authoritySetup(authority.getAuthorityID(), gp, attribute);

        PublicKey pk = ak.getPublicKeys().get(attribute);
        System.out.println("Adicionando novo atributo...");
        contract.submitTransaction("addPublicKey", authKey, attribute, Util.getBase64Eg1g1ai(pk.getEg1g1ai()),
                Util.getBase64G1yi(pk.getG1yi()));

        return ak;
    }

    private static GlobalParameters getGP(Contract contract, String key) throws ContractException {
        Instant startQueryGP = Instant.now();
        byte[] result = contract.evaluateTransaction("queryGlobalParameter", key);
        GlobalParameters gp = Util.readObjFromJSON(result, GlobalParameters.class);

        Instant endQueryGP = Instant.now();
        long totalQueryGP = Duration.between(startQueryGP, endQueryGP).toMillis();
        //System.out.println("Query gp\t" + totalQueryGP);

        return gp;
    }

    private static Authority getAuthority(Contract contract, String key) throws ContractException {
        Instant startQueryAuth = Instant.now();

        byte[] result = contract.evaluateTransaction("queryAuthority", key);

        Authority au = Util.readObjFromJSON(result, Authority.class);

        Instant endQueryAuth = Instant.now();
        long totalQueryAuth = Duration.between(startQueryAuth, endQueryAuth).toMillis();
        //System.out.println("Query auth: " + totalQueryAuth);

        return au;
    }

    private static User getUser(Contract contract, String key) throws ContractException {
        Instant startQueryUser = Instant.now();

        byte[] result = contract.evaluateTransaction("queryUser", key);
        User us = Util.readObjFromJSON(result, User.class);

        Instant endQueryUser = Instant.now();
        long totalQueryUser = Duration.between(startQueryUser, endQueryUser).toMillis();
        //System.out.println("Query user\t" + totalQueryUser);

        return us;
    }

    private static MedRecord getMedRecord(Contract contract, String key) throws ContractException {
        Instant startQueryMedRecord = Instant.now();

        byte[] result = contract.evaluateTransaction("queryMedRecord", key);
        MedRecord mr = Util.readObjFromJSON(result, MedRecord.class);

        Instant endQueryMedRecord = Instant.now();
        long totalQueryMedRecord = Duration.between(startQueryMedRecord, endQueryMedRecord).toMillis();
        //System.out.println("Query MedRecord\t" + totalQueryMedRecord);

        return mr;
    }

    private static void sendRecordToS3(byte[] data, String userID, String fileName) {
        InputStream is = new ByteArrayInputStream(data);
        // Upload a file as a new object with ContentType and title specified.
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/pdf");
        metadata.addUserMetadata("title", "someTitle");
        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, userID + "/" + fileName, is, metadata);
        s3Client.putObject(request);
    }

    private static byte[] getRecordFromS3(String userID, String fileName) throws IOException {
        S3Object s3object = s3Client.getObject(BUCKET_NAME, userID + "/" + fileName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        return IOUtils.toByteArray(inputStream);
    }

    private static List<String> getAllUserMedRecords(String userID) {
        ObjectListing listing = s3Client.listObjects(BUCKET_NAME, userID);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3Client.listNextBatchOfObjects(listing);
            summaries.addAll(listing.getObjectSummaries());
        }

        return summaries.stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    private static MedRecord createMedRecord(Contract contract, byte[] clearFile, User user, String policy, GlobalParameters gp,
                                             Authority authority, String gpId) throws InterruptedException, InvalidCipherTextException, IOException, TimeoutException,
            ContractException, NoSuchAlgorithmException {
        Instant startEncFull = Instant.now();
        byte[] encPdf = ABEUtil.encrypt(clearFile, policy, gp, authority);
        Instant endEncFull = Instant.now();
        long total = Duration.between(startEncFull, endEncFull).toMillis();
        //System.out.println("File full encrypt\t" + total);

        Instant startHash = Instant.now();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(encPdf);
        String encodedHash = Util.encodeBytesToBase64(hash);
        Instant endHash = Instant.now();
        long totalHash = Duration.between(startHash, endHash).toMillis();
        //System.out.println("Hash: " + totalHash);

        Instant startS3 = Instant.now();
        String fileName = Long.toString(timestamp.getTime()) + encodedHash.substring(0, 5);
        sendRecordToS3(encPdf, user.getId(), fileName);
        //Instant endS3 = Instant.now();
        //long totalS3 = Duration.between(startS3, endS3).toMillis();
        //System.out.println("S3: " + totalS3);

        Instant startMedRecord = Instant.now();
        // Todo: criar modelo para o gp para incluir chave
        MedRecord md = createMedRecord(contract, fileName, user.getId(), fileName, encodedHash, authority.getAuthorityID(), gpId);
        System.out.println(fileName);
        Instant endMedRecord = Instant.now();
        long totalMedRecord = Duration.between(startMedRecord, endMedRecord).toMillis();
        //System.out.println("MedRecord to blockchain\t" + totalMedRecord);
        //System.out.println("filename    " + fileName);

        return md;
    }

    // Todo: Usuário está sendo criado aqui. Isso não faz sentido se o cliente é de uso público
    //       pois posso fingir ser quem eu quiser.

    // Todo: Pensando melhor, permitir a criação de usuários tambem seria um problema, então o cliente
    //       talvez não deva ser público.

    //Todo: Talvez este seja apenas um novo caso de uso
    private static MedRecord getMedRecordFile(Contract contract, String medRecordID, String gpID, String userId, String filePath, int numericId)
            throws ContractException, IOException, InvalidCipherTextException, ClassNotFoundException {

        MedRecord mr = getMedRecord(contract, medRecordID);
        User user = getUser(contract, userId);
        GlobalParameters gp = getGP(contract, gpID);

        byte[] cypheredPdf = getRecordFromS3(mr.getUserId(), mr.getFileName());
        //Instant startReadFile = Instant.now();
        //byte[] cypheredPdf = FileUtils.readFileToByteArray(new File("ciphered-sample.pdf"));
        //Instant endReadFile = Instant.now();
        //long timeRead = Duration.between(startReadFile, endReadFile).toMillis();
        //System.out.println("leitura do arquivo\t " + timeRead);

        Instant startKeys = Instant.now();
        PersonalKeys perKeys = new PersonalKeys(user.getId());

        for (String attribute : user.getAttributes()) {
            SecretKey sk = Util.readAuthoritySecretKey(mr.getAuthorityID(), attribute);
            perKeys.addKey(DCPABE.keyGen(user.getId(), attribute, sk, gp));
        }
        Instant endKeys = Instant.now();
        long timeKeys = Duration.between(startKeys, endKeys).toMillis();
        //System.out.println("key management\t " + timeKeys);

        byte[] decryptedPdf = ABEUtil.decrypt(cypheredPdf, perKeys, gp, user.getAttributes().toArray(new String[]{}));
        Instant startWriteFile = Instant.now();
        FileUtils.writeByteArrayToFile(new File(filePath), decryptedPdf);
        Instant endWriteFile = Instant.now();
        long timeWrite = Duration.between(startWriteFile, endWriteFile).toMillis();
        //System.out.println("escrita do arquivo\t " + timeWrite);
        return mr;
    }
}
