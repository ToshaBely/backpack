import java.util.*;

public class Main {
    public static void main(String[] args) {
        new BackpackWorker().doWork();
    }
}

class BackpackItem {
    private long weight;
    private long price;

    BackpackItem(long weight, long price) {
        this.weight = weight;
        this.price = price;
    }

    long getWeight() {
        return weight;
    }

    long getPrice() {
        return price;
    }
}

class RightBackpackItem extends BackpackItem {
    private int mask;
    private long maxPriceAbove;
    private int maxMaskAbove;

    RightBackpackItem(long weight, long price, int mask) {
        super(weight, price);
        this.mask = mask;
    }

    int getMask() {
        return mask;
    }

    long getMaxPriceAbove() {
        return maxPriceAbove;
    }

    void setMaxPriceAbove(long maxPriceAbove) {
        this.maxPriceAbove = maxPriceAbove;
    }

    int getMaxMaskAbove() {
        return maxMaskAbove;
    }

    void setMaxMaskAbove(int maxMaskAbove) {
        this.maxMaskAbove = maxMaskAbove;
    }
}

class LeftBackpackItem extends BackpackItem {
    private int mask;

    LeftBackpackItem(long weight, long price, int mask) {
        super(weight, price);
        this.mask = mask;
    }

    int getMask() {
        return mask;
    }
}

class BackpackWorker {
    private int leftCount;
    private int rightCount;

    private long maxWeight;

    private List<BackpackItem> items = new ArrayList<>();
    private List<LeftBackpackItem> leftItems = new ArrayList<>();
    private List<RightBackpackItem> rightItems = new ArrayList<>();

    private List<Integer> twoPowList = new ArrayList<>();

    private List<Integer> resultList = new ArrayList<>();

    void doWork() {
        this.inputData();
        this.createTwoPowList();
        this.decomposeItemList();
        this.setMaxPriceAbove();
        this.pushResult();

        int size = this.resultList.size();
        System.out.println(size);
        for (int i = 0; i < size; i++) {
            System.out.print(this.resultList.get(i));
            System.out.print(' ');
        }
    }

    private void inputData() {
        Scanner scanner = new Scanner(System.in);

        int count = scanner.nextInt();
        this.leftCount = count / 2;
        this.rightCount = count - this.leftCount;

        this.maxWeight = scanner.nextLong();

        for(int i = 0; i < count; i++) {
            BackpackItem item = new BackpackItem(scanner.nextLong(), scanner.nextLong());
            this.items.add(item);
        }
    }

    private void createTwoPowList() {
        for (int i = 0; i <= this.rightCount; i++) {
            this.twoPowList.add((int) Math.pow(2., i));
        }
    }

    private void decomposeItemList() {
        for (int i = 0; i < this.twoPowList.get(this.rightCount); i++) {
            long sumWeight = 0;
            long sumPrice = 0;

            for (int j = 0; j < this.rightCount; j++) {
                if ((i & this.twoPowList.get(j)) != 0) {
                    BackpackItem oldItem = this.items.get( this.leftCount + j );
                    sumWeight += oldItem.getWeight();
                    sumPrice += oldItem.getPrice();
                }
            }

            this.rightItems.add(new RightBackpackItem(sumWeight, sumPrice, i));
        }

        this.rightItems.sort(
            (RightBackpackItem a, RightBackpackItem b) ->
                a.getWeight() == b.getWeight() ? 0 : a.getWeight() - b.getWeight() > 0 ? 1 : -1);

        for (int i = 0; i < this.twoPowList.get(this.leftCount); i++) {
            long sumWeight = 0;
            long sumPrice = 0;

            for (int j = 0; j < this.leftCount; j++) {
                if ((i & this.twoPowList.get(j)) != 0) {
                    BackpackItem oldItem = this.items.get(j);
                    sumWeight += oldItem.getWeight();
                    sumPrice += oldItem.getPrice();
                }
            }

            this.leftItems.add(new LeftBackpackItem(sumWeight, sumPrice, i));
        }
    }

    private void setMaxPriceAbove() {
        long maxPrice = this.rightItems.get(0).getPrice();
        int maskMaxPrice = 0;

        for (int i = 1; i < this.rightItems.size(); i++) {
            RightBackpackItem item = this.rightItems.get(i);

            if (item.getPrice() > maxPrice) {
                maxPrice = item.getPrice();
                maskMaxPrice = item.getMask();
            }

            item.setMaxPriceAbove(maxPrice);
            item.setMaxMaskAbove(maskMaxPrice);
        }
    }

    private int getNearestByWeight(long weigth) {
        return binSearch(0, this.rightItems.size() - 1, weigth);
    }

    private int binSearch(int left, int right, long val) {
        if (left < right) {
            int mid = (left + right) / 2;
            long itemWeight = this.rightItems.get(mid).getWeight();
            return itemWeight == val ? mid
                    : itemWeight < val ? binSearch(mid + 1, right, val)
                        : binSearch(left, mid - 1, val);
        } else {
            // left == right
            return this.rightItems.get(left).getWeight() > val ? left - 1 : left;
        }
    }

    private void pushResult() {
        long resultPrice = 0;
        int resultLeftIndex = 0;
        int resultRightIndex = 0;

        for (int i = 0; i < this.leftItems.size(); i++) {
            if (this.leftItems.get(i).getWeight() > this.maxWeight) {
                continue;
            }

            long restWeight = this.maxWeight - this.leftItems.get(i).getWeight();
            long sumPrice = this.leftItems.get(i).getPrice();

            int rightIndex = this.getNearestByWeight(restWeight);
            sumPrice += this.rightItems.get(rightIndex).getMaxPriceAbove();

            if (sumPrice > resultPrice) {
                resultPrice = sumPrice;
                resultLeftIndex = i;
                resultRightIndex = rightIndex;
            }
        }

        int leftMask = this.leftItems.get(resultLeftIndex).getMask();
        int rightMask = this.rightItems.get(resultRightIndex).getMaxMaskAbove();

        this.pushIndexes(leftMask, true);
        this.pushIndexes(rightMask, false);
    }

    private void pushIndexes(int mask, boolean isLeft) {
//        int mask = isLeft ? ((LeftBackpackItem) item).getMask() : ((RightBackpackItem) item).getMask();
        int degree = isLeft ? this.leftCount : this.rightCount;

        for (int i = 0; i < degree; i++) {
            if ((mask & this.twoPowList.get(i)) != 0) {
                this.resultList.add((isLeft ? i : i + this.leftCount) + 1);
            }
        }
    }
}
