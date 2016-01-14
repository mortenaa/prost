package no.imr.barents.prost;

public class HaddockRecruitmentGenerator extends RecruitmentGenerator {

    double p;
    int t=-1;
    RecruitmentGenerator low,good,outstanding;

    void readInput(InputReader in) {
        in.expectWord("low-recruitment");
        low=new RickerRecruitmentGenerator();
        low.readInput(in);
        low.readError(in);
        in.expectWord("good-recruitment");
        good=new RickerRecruitmentGenerator();
        good.readInput(in);
        good.readError(in);
        in.expectWord("outstanding-recruitment");
        p=in.expectDouble("p");
        if(p<0 || p>1)
            in.error("p should be between 0 and 1");
        outstanding=new OckhamRecruitmentGenerator();
        outstanding.readInput(in);
        outstanding.readError(in);
    }

    public double generate(StockModel s, int y) {
        t=(t+1)%7;
        //System.out.println("Haddock recruitment, index: "+t);
        if(t<4) {
            /*System.out.println(" low:  "+ low.generate(s,y));*/ 
            return(low.generate(s,y));
        } else if(t==4) {
            /*System.out.println(" good: "+ good.generate(s,y));*/ 
            return(good.generate(s,y));
        } else if(t==5) {
            if(RandomGenerator.nextDouble()<p) {
                /*System.out.println(" outs: "+ outstanding.generate(s,y));*/
                return(outstanding.generate(s,y));
            } else {
                /*System.out.println(" good: "+ good.generate(s,y));*/ 
                return(good.generate(s,y));
            }
            
        } else if(t==6) {
            /*System.out.println(" good: "+ good.generate(s,y));*/
            return(good.generate(s,y));
        }
        System.out.println("out of cheese error.");
        System.exit(1);
        return 0.0;
    }
    
    public void reset() {
        t=-1;
    }
}

