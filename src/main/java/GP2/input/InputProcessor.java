package GP2.input;

import GP2.input.FTType.FromToType;
import GP2.utils.Constants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class InputProcessor {
    // Member
    public EnumMap<FromToType, _WhoFromTo> _Input = new EnumMap<>(FromToType.class);

    // Functions
    private void addItem(FromToType key, _NameAmount nameAmount) {
        _WhoFromTo whoFromTo = _Input.computeIfAbsent(key, k -> new _WhoFromTo());
        whoFromTo.addItem(nameAmount);
    }

    private float getTotalAmount(FromToType key) {
        _WhoFromTo whoFromTo = _Input.get(key);
        if (whoFromTo == null) return 0;

        return (float) whoFromTo._Collection.stream()
                .filter(na -> na.m_amount != null)
                .mapToDouble(na -> na.m_amount)
                .sum();
    }

    private void assignAmount(FromToType key, float totalAmount) {
        _WhoFromTo whoFromTo = _Input.get(key);
        if (whoFromTo == null) return;

        float remainingAmount = totalAmount
                - getTotalAmount(FromToType.All)
                - getTotalAmount(FromToType.Remainder)
                - getTotalAmount(FromToType.Individual);

        whoFromTo._Collection.stream()
                .filter(na -> na.m_amount == null)
                .forEach(na -> na.m_amount = remainingAmount);
    }

    private void assignAmounts(float totalAmount) {
        assignAmount(FromToType.All, totalAmount);
        assignAmount(FromToType.Remainder, totalAmount);
        assignAmount(FromToType.Individual, totalAmount);

        float totalAssigned = getTotalAmount(FromToType.All)
                + getTotalAmount(FromToType.Remainder)
                + getTotalAmount(FromToType.Individual);

        float difference = totalAmount - totalAssigned;
        float tolerance = 0.01f; // 1 cent tolerance
        if (Math.abs(difference) >= tolerance) {
            System.out.println("Amounts do not tally: " + totalAmount + " <> " + totalAssigned + ", diff: " + difference);
        }
    }

    private int getIndivNullCount() {
        _WhoFromTo whoFromTo = _Input.get(FromToType.Individual);
        if (whoFromTo == null) return 0;

        return (int) whoFromTo._Collection.stream()
                .filter(na -> na.m_amount == null)
                .count();
    }

    private int putIndivAmount(float amount, int count) {
        _WhoFromTo whoFromTo = _Input.get(FromToType.Individual);
        if (whoFromTo == null) return 0;

        whoFromTo._Collection.stream()
                .filter(na -> na.m_amount == null)
                .forEach(na -> na.m_amount = amount);

        whoFromTo._Count += count;
        return whoFromTo._Count;
    }

    private void assignIndivAmounts(float totalAmount) {
        if (_Input.get(FromToType.All) == null && _Input.get(FromToType.Remainder) == null) {
            int nullCount = getIndivNullCount();
            if (nullCount != 0) {
                float indivEachAmount = (totalAmount - getTotalAmount(FromToType.Individual)) / nullCount;
                putIndivAmount(indivEachAmount, nullCount);
            }
        }
    }

    private void assignCounts(int activeCount) {
        /*
        1. Iterate (_INDIV), if (m_amount or name == null) count = 0;
        2. Iterate (_REM), count = (sum(active.persons) - sum(_INDIV));
        3. Iterate (_ALL), count = sum(active.persons);
        */
        // _INDIV
        _WhoFromTo whoFromTo = _Input.get(FromToType.Individual);
        if (whoFromTo != null) {
            whoFromTo._Count = (int) whoFromTo._Collection.stream()
                    .filter(na -> na.m_amount != null)
                    .count();
        }

        // _REM
        whoFromTo = _Input.get(FromToType.Remainder);
        if (whoFromTo != null) {
            whoFromTo._Count = activeCount - whoFromTo._Count;
        }

        // _ALL
        whoFromTo = _Input.get(FromToType.All);
        if (whoFromTo != null) {
            whoFromTo._Count = activeCount;
        }
    }

    private void processFrToExtended(float totalAmount, int totalActive) {
        /*
        1. Skip all entries in _UNKNOWN
        2. For _INDIV entries, if null amount, calculate each amount
        3. If (total_amount != sum(all,rem,indiv)) = error(amounts do not tally)
        4. Balance = total_amount - sum(all,rem,indiv). Assign balance in order (of those specified): (all,rem,indiv)
        */
        assignCounts(totalActive);
        assignIndivAmounts(totalAmount);
        assignAmounts(totalAmount);
        assignCounts(totalActive); // Reassign counts as amounts could have changed this.
    }

    public void processFrTo(String item, int idx, float amt, int activeCount, String input) {
        String[] inputs = input.split(Constants._ITEM_SEPARATOR);
        for (String s : inputs) {
            _NameAmount nameAmount = splitNameAmount(s);

            if (s.contains(FTType.FromToTypes.All)) {
                addItem(FromToType.All, nameAmount);
            } else if (s.contains(FTType.FromToTypes.Rem)) {
                addItem(FromToType.Remainder, nameAmount);
            } else if (nameAmount.m_name == null) {
                addItem(FromToType.Unknown, nameAmount);
            } else {
                addItem(FromToType.Individual, nameAmount);
            }
        }
        processFrToExtended(amt, activeCount);
    }

    private _NameAmount splitNameAmount(String stringToSplit) {
        String[] parts = stringToSplit.split(Constants._AMT_INDICATOR);
        String name = null;
        Float amount = null;

        for (String part : parts) {
            try {
                amount = Float.valueOf(part);
            } catch (NumberFormatException e) {
                name = part.trim();
            }
        }
        return new _NameAmount(name, amount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        _Input.forEach((key, whoFromTo) -> {
            sb.append(key).append("\n").append(whoFromTo.toString()).append("\n");
        });
        return sb.toString();
    }

    // --------------------------------------------------------------------------
    public class _WhoFromTo {
        public int _Count = 0;
        public List<_NameAmount> _Collection = new ArrayList<>();

        private void addItem(_NameAmount nameAmount) {
            _Collection.add(new _NameAmount(nameAmount.m_name, nameAmount.m_amount));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Count::[").append(_Count).append("]");
            _Collection.forEach(na -> sb.append(na.toString()));
            return sb.toString();
        }
    }

    // --------------------------------------------------------------------------
    public class _NameAmount {
        public String m_name;
        public Float m_amount;

        private _NameAmount(String name, Float amount) {
            this.m_name = name;
            this.m_amount = amount;
        }

        @Override
        public String toString() {
            return "\tNameAmount::[" + m_name + "][" + m_amount + "]";
        }
    }
}