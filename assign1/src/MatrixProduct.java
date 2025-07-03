public class MatrixProduct {
    public static void main(String[] args) {

        int m_ar = 4000;
        int m_br = 4000;

        double time = onMult(m_ar, m_br);
        double time2 = OnMultLine(m_ar,m_br);
        System.out.println("Time: " + time + " seconds");
        System.out.println("Time2 " + time2 + " seconds");
    }

    // l_mar c_mar l_mb c_mb  = l_mar c_mb
    public static double onMult(int m_ar, int m_br){
        double [] pha = new double [m_ar*m_ar];
        double [] phb = new double [m_ar*m_ar];
        double [] phc = new double [m_ar*m_ar];

        for ( int i =0; i < m_ar; i++){
            for (int j = 0; j < m_ar; j++){
                pha[i*m_ar+j] = 1;
            }
        }

        for ( int i =0; i < m_br; i++){
            for (int j = 0; j < m_br; j++){
                phb[i*m_br+j] = i+1;
            }
        }

        long Time1 = System.currentTimeMillis();

        for(int i=0; i<m_ar; i++)
        {	for( int j=0; j<m_br; j++)
        {	double  temp = 0;
            for( int k=0; k<m_ar; k++)
            {
                temp += pha[i*m_ar+k] * phb[k*m_br+j];
            }
            phc[i*m_ar+j]=temp;
        }
        }

        long Time2 = System.currentTimeMillis();

        double Final_time = (Time2 - Time1) / 1000.0;
        System.out.printf("Time: %.3f seconds%n%n", Final_time);

        System.out.println("Result Matrix: ");
        for(int i = 0; i < 1; i++){
            for(int j = 0; j < Math.min(10,m_ar); j++){
                System.out.println(phc[i*m_ar+j] + " ");
            }
            System.out.println();
        }

        return Final_time;


    }

    public static double OnMultLine (int m_ar, int m_br){

        double [] pha = new double [m_ar*m_ar];
        double [] phb = new double [m_ar*m_ar];
        double [] phc = new double [m_ar*m_ar];

        for ( int i =0; i < m_ar; i++){
            for (int j = 0; j < m_ar; j++){
                pha[i*m_ar+j] = 1;
            }
        }

        for ( int i =0; i < m_br; i++){
            for (int j = 0; j < m_br; j++){
                phb[i*m_br+j] = i+1;
            }
        }

        long Time1 = System.currentTimeMillis();

        for ( int i = 0 ; i < m_ar; i++){
           for ( int k = 0 ; k < m_ar; k++){
               double temp = pha[i*m_ar+k];
               for(int j =  0; j < m_br; j++){
                   phc[i*m_ar+j] += temp*phb[k*m_br+j];
               }
           }
        }

        long Time2 = System.currentTimeMillis();

        double Final_time = (Time2 - Time1) / 1000.0;
        System.out.printf("Time: %.3f seconds%n%n", Final_time);

        System.out.println("Result Matrix: ");
        for(int i = 0; i < 1; i++){
            for(int j = 0; j < Math.min(10,m_ar); j++){
                System.out.println(phc[i*m_ar+j] + " ");
            }
            System.out.println();
        }

        return Final_time;


    }
}