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


 
public class MyDtVovsAthHisEbc {

	
	
	 /* 마이데이터 해외 가맹점명 6개월 이전*/
	public LData retvMerchantName(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtVovsAthHisEbc/retvMerchantName", input);
		return dao.executeQueryForSingle();
	}
	 /* 마이데이터 국제전표기본에서 해외 가맹점명 조회*/
	public LData retvSlipMerchantName(LData input) throws DevonException {
		LCommonDao dao = new LCommonDao("card/MyDtVovsAthHisEbc/retvSlipMerchantName", input);
		return dao.executeQueryForSingle();
	}
	
	 /* 마이데이터 GBF_BC카드전표기본 에서 해외 가맹점명 조회*/
	public LData retvBcMerchantName(LData input) throws DevonException{
		LCommonDao dao = new LCommonDao("card/MyDtVovsAthHisEbc/retvBcMerchantName", input);
		return dao.executeQueryForSingle();
	}
	
	 /* 마이데이터 GBF국제전표기본 에서 미화매입금액 조회*/
	public LData retvUsdAmt(LData input) throws DevonException{
		LCommonDao dao = new LCommonDao("card/MyDtVovsAthHisEbc/retvUsdAmt", input);
		return dao.executeQueryOnlySingle();
	}
	
	 /* 마이데이터 GBC매출전표상세 에서 미화매입금액 조회*/
	public LData retvUsdAmt2(LData input) throws DevonException{
		LCommonDao dao = new LCommonDao("card/MyDtVovsAthHisEbc/retvUsdAmt2", input);
		return dao.executeQueryOnlySingle();
	}
}

