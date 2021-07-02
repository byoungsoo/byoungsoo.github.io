package com.kbcard.ubd.ebi.card;

import java.math.BigDecimal;

import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.config.ConfigurationUtil;
import devon.core.config.LConfiguration;
import devon.core.context.ContextHandler;
import devon.core.context.ContextKey;
import devon.core.exception.DevonException;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devon.core.util.LDateUtils;
import devon.core.util.LNullUtils;
import devonenterprise.channel.controller.helper.ControllerCommonHelper;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD_CODE;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD_CODE.SDT_CHN_DTCD;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD_CODE.SDT_CHN_DTLS_BWK_DTCD;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD_CODE.SDT_DAT_SECT_ECY_YN;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD_CODE.SDT_IN_TLG_PT_DTCD;
import devonenterprise.ext.channel.telegram.KBCDSYSTEM_HEADERFIELD_CODE.SDT_LANG_DTCD;
import devonenterprise.ext.channel.telegram.util.HeaderUtil;
import devonenterprise.ext.core.context.ExtensionContextKey.HEADER_SPEC;
import devonenterprise.ext.core.exception.LBizException;
import devonframework.persistent.autodao.LCommonDao;

/**
 * 
 *
 * @logicalName   마이데이터고객포인트잔여조회Pbi
 * @lastDate      2021-05-31
 */
public class MydtCstPntRmgInqEbc {
	
	/** 포인트고객조회 */
	public LData selectPointUsr(LData iDao) throws DevonException {
		LCommonDao rDao = new LCommonDao("card/MyDtCrdPntInfEbc/selectPntLst", iDao);
		return rDao.executeQueryForSingle();
	}
	
	/** 준회원기본조회ByCI내용조회 */
	public LData retvSemiMbrBasInqByCiCtt(String ci) throws DevonException {
		LData iDao = new LData();
		iDao.setString("CI내용", ci);
		LCommonDao rDao = new LCommonDao("card/MyDtCrdPntInfEbc/selectSemiMbrBasInqByCiCtt", iDao);
		return rDao.executeQueryForSingle();
	}

	/** CI연계번호고객식별자조회 */
	public LData retvCiLnkdNoCsid(String ci) throws DevonException {
		LData iDao = new LData();
		iDao.setString("CI내용", ci);
		LCommonDao rDao = new LCommonDao("card/MyDtCrdPntInfEbc/selectCiLnkdNoCsid", iDao);
		return rDao.executeQueryForSingle();
	}
	
	/** 포인트잔여기본By포인트사용그룹조회 */
	public LData retvPntRmgBasByPntUseGrp( LData iDao ) throws DevonException {
		LCommonDao rDao = new LCommonDao("card/MyDtCrdPntInfEbc/selectPntRmgBasByPntUseGrp", iDao);
		return rDao.executeQueryForSingle();
	}
	
	/** 포인트월누적기본By소멸예정집계목록조회 */
	public LMultiData retvLstPntMnAcmBasByExtgSchSmm( LData iDao ) throws DevonException {
		LCommonDao rDao = new LCommonDao("card/MyDtCrdPntInfEbc/selectLstPntMnAcmBasByExtgSchSmm", iDao);
		return rDao.executeQuery();
	}
	
}



