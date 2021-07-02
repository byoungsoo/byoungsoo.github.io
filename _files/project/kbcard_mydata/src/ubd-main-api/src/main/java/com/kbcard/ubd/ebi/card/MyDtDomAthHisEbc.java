package com.kbcard.ubd.ebi.card;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.DevonException;
import devon.core.log.LLog;
import devonenterprise.ext.persistent.dao.LPagingCommonDao;
import devonframework.persistent.autodao.LCommonDao;
/**
 * 프로그램명 		: 마이데이터 카드목록조회 query
 * 작성자 			: 김민성
 * 작성일자 		: 2021-06-10 
 * 설      명 		: 마이데이터 카드목록조회 Ebc 
 */
 
public class MyDtDomAthHisEbc {

	  /*카드대체번호대체카드상세단건조회*/
	public LData retvGagAltrCrdDtlByCrdAltrNo(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtDomAthHisEbc/retvGagAltrCrdDtlByCrdAltrNo", input);
		return dao.executeQueryOnlySingle();
	}
	
	/* 카드상세조회 */
	public LData retvGagCrdDtl( LData input ) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtDomAthHisEbc/retvGagCrdDtl", input);
		return dao.executeQueryOnlySingle();
	}
	
	/* 마이데이터승인내역목록조회 6개월 이전 */
	public LMultiData retvLstMydtAthHis(LData input) throws DevonException {
		LPagingCommonDao dao = new LPagingCommonDao("card/MyDtDomAthHisEbc/retvLstMydtAthHis", input);
		return dao.executeQueryForScrollPage();
	}
	

	 /* 마이데이터과거승인내역목록조회 6개월 이후*/
	public LMultiData retvLstMydtPstAthHis(LData input) throws DevonException {
		LPagingCommonDao dao = new LPagingCommonDao("card/MyDtDomAthHisEbc/retvLstMydtPstAthHis", input);
		return dao.executeQueryForScrollPage();
	}
	
	  /*카드금융제휴기관구분코드 070(국민카드) 가맹점명 조회*/
	public LData retvGdbMmcBas(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtDomAthHisEbc/retvGdbMmcBas", input);
		return dao.executeQueryOnlySingle();
	}
	  /*카드금융제휴기관구분코드 098(비씨) 가맹점명 조회*/
	public LData retvGDB_BcMmcBas(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtDomAthHisEbc/retvGDB_BcMmcBas", input);
		return dao.executeQueryOnlySingle();
	}
	
	
	/* 카드금융제휴기관구분코드 공동가맹점 가맹점명 조회 */
	public LMultiData retvLstGdbFnbbMmc( LData iDao ) throws DevonException {
		LCommonDao rDao = new LCommonDao("card/MyDtDomAthHisEbc/retvLstGdbFnbbMmc", iDao);
		return rDao.executeQuery();
		
	}
	
	
}

