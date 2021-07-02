package com.kbcard.ubd.cpbi.phs;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonframework.business.transaction.nested.LNestedTransactionManager;

/**
 * 프로그램명 	: 마이데이터 요청제공내역 관리
 * 처리사항 	: DevonFramework 거래선후처리클래스 등록함
 * 작성자 		: 임용택
 * 작성일자 	: 2021-06-03
 * 설      명 	: 마이데이터 요청제공내역 관리 cpbi
 */
public class DmdOferHisCpbc {
	
	// 요청제공내역 관리
	public Boolean regtMydtApiDmdOferHis(LData iDmdOferHis) throws LException {

		Boolean sRtn = false;
		
		sRtn = regtMydtApiDmdHis(iDmdOferHis);	// 마이데이터API요청내역등록
		
		sRtn = regtMydtApiOferHis(iDmdOferHis);	// 마이데이터API제공내역등록
		
		sRtn = regtMydtApiOferHisDtl(iDmdOferHis);	// 마이데이터API제공내역상세등록
		
		return sRtn;
		
	}
	
	/** 
	 * 마이데이터API요청내역등록
	 * 1. 마이데이터요청내역 조회
	 * 2. 마이데이터요청내역 등록
	 * @param iDmdOferHis
	 * @return Boolean = true/false
	 * @throws LException
	 */
	public Boolean regtMydtApiDmdHis(LData iDmdOferHis) throws LException {
		Boolean bRtn = false;
		int iRtn = 0;
		int iRtnHis = 0;

//		// 1. 마이데이터요청내역 삭제
//		iRtn = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "deleteMydtDmdHis", iDmdOferHis);
		
		// 1. 마이데이터요청내역 조회
		//    중복체크 로직 추가
		
    	// 01. transaction 분리를 위한 transaction manager 생성
        LNestedTransactionManager ntxManager = new LNestedTransactionManager();  
        try {
        	//02. transaction 분리 시작
            ntxManager.nestedBegin(); 
            
            //02. query수행 
    		LMultiData 	OData = new LMultiData();
    		OData = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "selectMydtDmdHis", iDmdOferHis);

    		if(OData.getDataCount() == 0) {
    			// 2. 마이데이터요청내역 등록
    			iRtnHis = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "insertMydtDmdHis", iDmdOferHis); 
    		}    		
            //03. commit 수행
            ntxManager.nestedCommit(); //
            
        } catch ( Throwable t ) {
            t.printStackTrace(LLog.err);
            //04. 오류 발생시 rollback 수행 
            ntxManager.nestedRollback();
            if ( t instanceof LException ) {
                throw (LException)t;
            } else {
                
            }            
        } finally {
        	//05. transaction manager 종료
        	ntxManager.nestedRelease();
        }
        
		return true;
	}

	/** 
	 * 마이데이터API제공내역등록
	 * 1. 마이데이터제공내역 조회
	 * 2. 마이데이터제공내역 등록
	 * @param iDmdOferHis
	 * @return Boolean = true/false
	 * @throws LException
	 */
	public Boolean regtMydtApiOferHis(LData iDmdOferHis) throws LException {
		Boolean bRtn = false;
		int iRtn = 0;
		int iRtnHis = 0;
		
//		// 1. 마이데이터제공내역 삭제
//		iRtn = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "deleteMydtOferHis", iDmdOferHis);
		
		// 1. 마이데이터제공내역 조회
		//   중복체크 로직 추가
		
    	// 01. transaction 분리를 위한 transaction manager 생성
        LNestedTransactionManager ntxManager = new LNestedTransactionManager();  
        try {
        	//02. transaction 분리 시작
            ntxManager.nestedBegin(); 
            
			LMultiData 	OData = new LMultiData();
			OData = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "selectMydtOferHis", iDmdOferHis);
			
			if(OData.getDataCount() == 0) {		
				// 2. 마이데이터제공내역 등록
				iRtnHis = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "insertMydtOferHis", iDmdOferHis); 
			}    		
	        //03. commit 수행
	        ntxManager.nestedCommit(); //
	        
	    } catch ( Throwable t ) {
	        t.printStackTrace(LLog.err);
	        //04. 오류 발생시 rollback 수행 
	        ntxManager.nestedRollback();
	        if ( t instanceof LException ) {
	            throw (LException)t;
	        } else {
	            
	        }            
	    } finally {
	    	//05. transaction manager 종료
	    	ntxManager.nestedRelease();
	    }
	    
		return true;
	}

	/** 
	 * 마이데이터API제공내역상세등록
	 * 1. 마이데이터제공내역상세 조회
	 * 2. 마이데이터제공내역상세 등록
	 * @param iDmdOferHis
	 * @return Boolean = true/false
	 * @throws LException
	 */	
	public Boolean regtMydtApiOferHisDtl(LData iDmdOferHis) throws LException {
		Boolean bRtn = false;
		int iRtn = 0;
		int iRtnHis = 0;
		
//		// 1. 마이데이터제공내역상세 삭제
//		iRtn = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "deleteMydtOferHisDtl", iDmdOferHis);
		
		//1. 마이데이터제공내역상세 조회
		//   중복체크 로직 추가
		
    	// 01. transaction 분리를 위한 transaction manager 생성
        LNestedTransactionManager ntxManager = new LNestedTransactionManager();  
        try {
        	//02. transaction 분리 시작
            ntxManager.nestedBegin(); 
            
			LMultiData 	OData = new LMultiData();
			OData = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "selectMydtOferHisDtl", iDmdOferHis);
			
			if(OData.getDataCount() == 0) {		
				// 2. 마이데이터제공내역상세 등록
				iRtnHis = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferHisEbc", "insertMydtOferHisDtl", iDmdOferHis); 
			}    		
	        //03. commit 수행
	        ntxManager.nestedCommit(); //
	        
	    } catch ( Throwable t ) {
	        t.printStackTrace(LLog.err);
	        //04. 오류 발생시 rollback 수행 
	        ntxManager.nestedRollback();
	        if ( t instanceof LException ) {
	            throw (LException)t;
	        } else {
	            
	        }            
	    } finally {
	    	//05. transaction manager 종료
	    	ntxManager.nestedRelease();
	    }
	    
		return true;
	}
 
}
