package GP2.input;

import GP2.utils.Constants ;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.Float;
import java.lang.Math;

public class InputProcessor extends Object {
	// member
	public HashMap<String, _WhoFromTo> 	_Input = null ;	// String/key = _REM | _ALL | _INDIV | _UNKNOWN

	// functions
	private void addItem (String k, _NameAmt na) {
		try {
			_WhoFromTo w = _Input.get(k) ;
			if (w == null) w = new _WhoFromTo(na) ;
			else w.addItem(na) ;
			_Input.put(k, w) ;
		} catch (Exception e) {
			System.err.println("Error:addItem::" + e.getMessage()) ;
		}
	}

	private float getTotalAmount (String key)
	{
		try {
			float fTotal = 0 ; 
			if (this._Input == null) return fTotal ;

			_WhoFromTo w = _Input.get(key) ;
			if (w == null) return fTotal ;

			Iterator<_NameAmt> iter ;
			iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				_NameAmt na = iter.next();
				if (na.m_amount != null) fTotal += na.m_amount ;
			}
			return fTotal ;
		} catch (Exception e) {
			System.err.println("Error:getTotalAmount::" + e.getMessage()) ;
			return 0 ; 
		}
	}

    private void assignAmount (String key, float totalAmount) {
		try {
			_WhoFromTo w = _Input.get(key) ;
			if (w == null) return ;

			Iterator<_NameAmt> iter ;
			iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				_NameAmt na = iter.next();
				if (na.m_amount == null) {
					na.m_amount = (totalAmount 
								- getTotalAmount(Constants._ALL_key) 
								- getTotalAmount(Constants._REM_key) 
								- getTotalAmount(Constants._INDIV_key)) ; 
					_Input.put(key, w) ;
				}
			}
		} catch (Exception e) {
			System.err.println("Error:assignAmount::" + e.getMessage()) ;
		}
	}	

    private void assignAmounts (float totalAmount) {
		try {
			if (this._Input == null) return ;
			assignAmount (Constants._ALL_key, totalAmount) ; 
			assignAmount (Constants._REM_key, totalAmount) ; 
			assignAmount (Constants._INDIV_key, totalAmount) ;
			
			float f = getTotalAmount(Constants._ALL_key) + getTotalAmount(Constants._REM_key) + getTotalAmount(Constants._INDIV_key) ;
			if (totalAmount != f) {
				float f1 = Math.abs(totalAmount-f) ;
				float f2 = 1/1000 ;	// tolerance	
				if ( f1 < f2 )
					System.out.println("Amounts do not tally: " + totalAmount + " <> " + f) ;
			}
		} catch (Exception e) {
			System.err.println("Error:assignAmounts::" + e.getMessage()) ;
		}
	}

    private int getIndivNullCount () {
		try {
			int nullCount = 0 ;
			if (this._Input == null) return nullCount ;

			String key = Constants._INDIV_key ;
			_WhoFromTo w = _Input.get(key) ;
			if (w == null) return nullCount ; 
			
			Iterator<_NameAmt> iter ;
			iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				_NameAmt na = iter.next();
				if (na.m_amount == null) nullCount++ ;
			}
			return nullCount ; 
		} catch (Exception e) {
			System.err.println("Error:getIndivNullCount::" + e.getMessage()) ;
			return 0 ;
		}
	}

    private int putIndivAmount (float nAmount, int nCount) {
		try {
			int nullCount = 0 ;
			if (this._Input == null) return nullCount ;

			String key = Constants._INDIV_key ;
			_WhoFromTo w = _Input.get(key) ;
			if (w == null) return nullCount ;
			
			Iterator<_NameAmt> iter ;
			iter = w._Collection.iterator() ;
			while (iter.hasNext()) {
				_NameAmt na = iter.next();
				if (na.m_amount == null) na.m_amount = nAmount ;
			}
			w._Count += nCount ;
			nullCount = w._Count ; 
			return nullCount ; 
		} catch (Exception e) {
			System.err.println("Error:putIndivAmount::" + e.getMessage()) ;
			return 0 ;
		}
	}

    private void assignIndivAmounts (float totalAmount) {
		try {
			if (this._Input == null) return ;

			if ( (_Input.get(Constants._ALL_key) == null) && (_Input.get(Constants._REM_key) == null) ) {
				int nNulls = getIndivNullCount() ;
				if (nNulls != 0) {
					float nIndivEachAmount = ((totalAmount-getTotalAmount(Constants._INDIV_key)) / nNulls) ;
					putIndivAmount (nIndivEachAmount, nNulls) ;
				}
			}
		} catch (Exception e) {
			System.err.println("Error:assignIndivAmounts::" + e.getMessage()) ;
		}
	}

	private void assignCounts (int nActive) {
		try {
			/*
			1. iterrate (_INDIV), if (amount or name == null) count = 0 ;
			2. iterrate (_ALL), count = sum(active.persons) ;
			3. iterrate (_REM), count = ( sum(active.persons) - sum(_INDIV) ) ;
			*/
			if (this._Input == null) return ;
			// _INDIV
			int indivTotal = 0 ;
			String k = Constants._INDIV_key ;
			_WhoFromTo w = _Input.get(k) ;
			if (w != null) {
				Iterator<_NameAmt> iter ;
				iter = w._Collection.iterator() ;
				while (iter.hasNext()) {
					_NameAmt na = iter.next();
					if (na.m_amount == null) {
						indivTotal = 0 ;
					}
					else {
						indivTotal++ ;
						w._Count = indivTotal ;
						_Input.put(k, w) ;
					}
				}
			}

			// _REM
			int numActive = nActive ;
			k = Constants._REM_key ; 
			w = _Input.get(k) ;
			if (w != null) {
				w._Count = (numActive - indivTotal) ;
				_Input.put(k, w) ;
			}

			// _ALL
			k = Constants._ALL_key ; 
			w = _Input.get(k) ;
			if (w != null) {
				w._Count = (numActive) ;
				_Input.put(k, w) ;
			}
		} catch (Exception e) {
			System.err.println("Error:assignCounts::" + e.getMessage()) ;
		}
	}

    private void processFrToExtended (float totalAmount, int totalActive) {
		try {
			/*
				1. skip all entries in _UNKNOWN
				2. for _INDIV entries, if null amount, calculate each amount
				3. if (total_amount != sum(all,rem,indiv)) = error(ammounts do not tally)
				4. balance = total_amount - sum(all,rem,indiv). Assign balance in order (of those specified): (all,rem,indiv)
			*/
			assignCounts(totalActive) ;
			assignIndivAmounts(totalAmount) ;
			assignAmounts(totalAmount) ;
			assignCounts(totalActive) ;		// reassign Counts as amounts could have changed this.
		} catch (Exception e) {
			System.err.println("Error:processFrToExtended::" + e.getMessage()) ;
		}
	}

    public void processFrTo (String item, int idx, float amt, int nActive, String sInput) {
		try{
			//System.out.println("==> " + item + "::[" + idx + "][" + amt + "][" + sInput + "]" );

			_NameAmt na = null ;
			String sIn[] = sInput.split(Constants._ITEM_SEPARATOR) ;
			for (int i = 0; i < sIn.length; i++) {
				na = splitNameAmount(sIn[i]) ;

				if (_Input == null) _Input = new HashMap<String, _WhoFromTo>();

				if (sIn[i].indexOf(Constants._ALL) != -1) {         // ALL found
					addItem(Constants._ALL_key, na);
				} else if (sIn[i].indexOf(Constants._REM) != -1) {  // REM found
					addItem(Constants._REM_key, na);
				} else if (na.m_name == null) {					// UNKNOWN
					addItem(Constants._UNKNOWN_key, na);
				} else {                                        // INDIV found
					addItem(Constants._INDIV_key, na);
				}
			}
			processFrToExtended(amt, nActive) ;
			//dumpCollection() ;
		} catch (Exception e) {
			System.err.println("Error:processFrTo::" + e.getMessage()) ;
		}
	}

    private _NameAmt splitNameAmount (String stringToSplit) {
        Float fAmt = null;
        String aName = null ;

		String sEach[] = stringToSplit.split(Constants._AMT_INDICATOR) ;
        for (int k = 0; k < sEach.length; k++) {
            try {
                fAmt = Float.valueOf(sEach[k]) ;
		    } catch (Exception e) {
                aName = new String(sEach[k].trim()) ;
            }
        }
        return (new _NameAmt(aName, fAmt)) ;
    }

	public void dumpCollection () {
		try {
			if (this._Input != null) {
				//System.out.println("dumpCollection ============") ;
				for(String key: _Input.keySet()) {
					System.out.println(key);
					_WhoFromTo w = _Input.get(key) ;
					w.dumpCollection() ;
				}
			}
		} catch (Exception e) {
			System.err.println("Error:dumpCollection::" + e.getMessage()) ;
		}
		return ;
	}

	// --------------------------------------------------------------------------
	public class _WhoFromTo {
		public int					_Count = 0 ;			// count(_Collection)
		public ArrayList<_NameAmt>	_Collection = null ;	// String/key = name of person. If _REM | _ALL, use literals.
													// float/amount = amount assigned.
		private _WhoFromTo(String s, Float f) {
			try {
				if (_Collection == null) _Collection = new ArrayList<_NameAmt>() ;
				_Collection.add(new _NameAmt(s, f)) ;
			} catch (Exception e) {
				//System.err.println("Error: " + e.getMessage()) ;
			}
		}

		private _WhoFromTo(_NameAmt na) {
			try {
				if (_Collection == null) _Collection = new ArrayList<_NameAmt>() ;
				_Collection.add(new _NameAmt(na)) ;
			} catch (Exception e) {
				//System.err.println("Error: " + e.getMessage()) ;
			}
		}

		private void addItem(_NameAmt na) {
			try {
				if (_Collection == null) _Collection = new ArrayList<_NameAmt>() ;
				_Collection.add(new _NameAmt(na.m_name, na.m_amount)) ;
			} catch (Exception e) {
				System.err.println("Error::addItem(na)::" + e.getMessage()) ;
			}
		}

		public void dumpCollection() {
			try {
				System.out.print("Count::[" + _Count + "]" );
				Iterator<_NameAmt> iter ;
				iter = this._Collection.iterator();
				while (iter.hasNext()) {
					_NameAmt na = iter.next();
					na.dumpCollection() ;
				}
				System.out.println("") ;
			} catch (Exception e) {
				System.err.println("Error::dumpCollection::" + e.getMessage()) ;
				return ;
			}
			return ;
		}
	}

	// --------------------------------------------------------------------------
	public class _NameAmt {
		public String m_name = null ;
		public Float m_amount = null ;
	
		private _NameAmt(String s, Float f) {
			if (s != null) m_name = new String(s) ;
			if (f != null) m_amount = new Float(f) ;
		}	
	
		private _NameAmt(_NameAmt na) {
			if (na.m_name != null) m_name = new String(na.m_name) ;
			if (na.m_amount != null) m_amount = new Float(na.m_amount) ;
		}	

		public void dumpCollection() {
            System.out.print("\t_NameAmt::["+ this.m_name + "][" + this.m_amount + "]") ;
			return ;
		}
	}
}