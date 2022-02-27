package hns.erse.hardware.states;

import org.ejml.data.*;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
import org.ejml.dense.row.mult.SubmatrixOps_DDRM;

import java.util.List;
import java.util.Vector;

public class CompassCalibration
{
    public double compensation_x_x;
    public double compensation_x_y;
    public double compensation_x_z;

    public double compensation_y_x;
    public double compensation_y_y;
    public double compensation_y_z;

    public double compensation_z_x;
    public double compensation_z_y;
    public double compensation_z_z;

    public double bias_x;
    public double bias_y;
    public double bias_z;

    public static CompassCalibration calc_calibration(double[][] data) {
        int m_length = data.length;
        SimpleMatrix matrix_d = new SimpleMatrix(10, m_length, MatrixType.DDRM);
        for(int i=0; i<m_length; i++) {
            matrix_d.set(0, i, data[i][0] * data[i][0]);
            matrix_d.set(1, i,  data[i][1] * data[i][1]);
            matrix_d.set(2, i,  data[i][2] * data[i][2]);
            matrix_d.set(3, i,  data[i][1] * data[i][2]);
            matrix_d.set(4, i,  data[i][0] * data[i][2]);
            matrix_d.set(5, i,  data[i][0] * data[i][1]);
            matrix_d.set(6, i,  2 * data[i][0]);
            matrix_d.set(7, i,  2 * data[i][1]);
            matrix_d.set(8, i,  2 * data[i][2]);
            matrix_d.set(9, i,  1);
        }

// System.out.println("matrix_d " + matrix_d);

        SimpleMatrix matrix_s = matrix_d.mult(matrix_d.transpose());

// System.out.println("matrix_s " + matrix_s);

        // setSubMatrix​(DMatrix1Row src, DMatrix1Row dst, int srcRow, int srcCol, int dstRow, int dstCol, int numSubRows, int numSubCols)

        SimpleMatrix matrix_s11 = new SimpleMatrix(6,6);
        SubmatrixOps_DDRM.setSubMatrix( matrix_s.getMatrix(), matrix_s11.getMatrix(), 0,0, 0,0, 6,6);

// System.out.println("matrix_s11 " + matrix_s11);

        SimpleMatrix matrix_s12 = new SimpleMatrix(  6,4);
        SubmatrixOps_DDRM.setSubMatrix(matrix_s.getMatrix(), matrix_s12.getMatrix(), 0,6, 0,0, 6,4);

// System.out.println("matrix_s12 " + matrix_s12);

        SimpleMatrix matrix_s12t = new SimpleMatrix(4,6);
        SubmatrixOps_DDRM.setSubMatrix(matrix_s.getMatrix(), matrix_s12t.getMatrix(), 6,0, 0,0, 4,6);

// System.out.println("matrix_s12t " + matrix_s12t);

        SimpleMatrix matrix_s22 = new SimpleMatrix(4,4);
        SubmatrixOps_DDRM.setSubMatrix(matrix_s.getMatrix(), matrix_s22.getMatrix(), 6,6, 0,0, 4, 4);

// System.out.println("matrix_s22 " + matrix_s22);

        SimpleMatrix matrix_s22_1 = matrix_s22.pseudoInverse();

        SimpleMatrix matrix_ss = matrix_s11.minus(matrix_s12.mult(matrix_s22_1).mult(matrix_s12t));

// System.out.println("matrix_ss " + matrix_ss);


        double[][] co_array = new double[][]{
                { -1.0,  1.0,  1.0,  0.0,  0.0,  0.0 },
                {  1.0, -1.0,  1.0,  0.0,  0.0,  0.0 },
                {  1.0,  1.0, -1.0,  0.0,  0.0,  0.0 },
                {  0.0,  0.0,  0.0, -4.0,  0.0,  0.0 },
                {  0.0,  0.0,  0.0,  0.0, -4.0,  0.0 },
                {  0.0,  0.0,  0.0,  0.0,  0.0, -4.0 }
        };

        SimpleMatrix matrix_co = new SimpleMatrix(co_array);

        SimpleMatrix matrix_c = matrix_co.invert();

        SimpleMatrix matrix_e = matrix_c.mult(matrix_ss);

        SimpleEVD<SimpleMatrix> eigen = matrix_e.eig();
        List<Complex_F64> wr = eigen.getEigenvalues();

        int index = 0;
        double maxval = wr.get(0).real;

        for(int i=0; i<6;i++) {
            if (wr.get(i).real > maxval) {
                maxval = wr.get(i).real;
                index = i;
            }
        }

        SimpleMatrix v1 = eigen.getEigenVector(index);
        if (v1.get(0) < 0.0) {
            v1.set(0,0, -v1.get(0,0));
            v1.set(1,0, -v1.get(1,0));
            v1.set(2,0, -v1.get(2,0));
            v1.set(3,0, -v1.get(3,0));
            v1.set(4,0, -v1.get(4,0));
            v1.set(5,0, -v1.get(5,0));
        }

        SimpleMatrix v2 = matrix_s22_1.mult(matrix_s12t).mult(v1);
        SimpleMatrix v = new SimpleMatrix(10,1);

        v.set(0, v1.get(0,0));
        v.set(1, v1.get(1,0));
        v.set(2, v1.get(2,0));
        v.set(3, v1.get(3,0));
        v.set(4, v1.get(4,0));
        v.set(5, v1.get(5,0));
        v.set(6, -v2.get(0,0));
        v.set(7, -v2.get(1,0));
        v.set(8, -v2.get(2,0));
        v.set(9, -v2.get(3,0));

        SimpleMatrix matrix_q = new SimpleMatrix(3, 3);
        matrix_q.set(0, 0, v.get(0,0));
        matrix_q.set(0, 1, v.get(5,0));
        matrix_q.set(0, 2, v.get(4,0));
        matrix_q.set(1, 0, v.get(5,0));
        matrix_q.set(1, 1, v.get(1,0));
        matrix_q.set(1, 2, v.get(3,0));
        matrix_q.set(2, 0, v.get(4,0));
        matrix_q.set(2, 1, v.get(3,0));
        matrix_q.set(2, 2, v.get(2,0));


        SimpleMatrix vector_u = new SimpleMatrix(3,1);
        vector_u.set(0, 0, v.get(6,0));
        vector_u.set(1, 0, v.get(7,0));
        vector_u.set(2, 0, v.get(8,0));

        SimpleMatrix matrix_q_1 = matrix_q.invert();

        SimpleMatrix matrix_b = matrix_q_1.mult(vector_u);

// System.out.println("Matrix B " + matrix_b.toString());

        matrix_b.set(0,0, -matrix_b.get(0,0));
        matrix_b.set(1,0, -matrix_b.get(1,0));
        matrix_b.set(2,0, -matrix_b.get(2,0));

        CompassCalibration compassCalibration = new CompassCalibration();
        compassCalibration.bias_x = matrix_b.get(0,0);
        compassCalibration.bias_y = matrix_b.get(1,0);
        compassCalibration.bias_z = matrix_b.get(2,0);

// System.out.println("bias_x " + compassCalibration.bias_x);
// System.out.println("bias_y " + compassCalibration.bias_y);
// System.out.println("bias_z " + compassCalibration.bias_z);

// System.out.println("matrix_b " + matrix_b);
// System.out.println("matrix_q " + matrix_q);
        SimpleMatrix matrix_btqb_1 = matrix_q.mult(matrix_b);
// System.out.println("matrix_btqb_1 " + matrix_btqb_1);

        SimpleMatrix matrix_btqb = new SimpleMatrix(1,3);
        matrix_btqb.set(0,0, matrix_btqb_1.get(0,0));
        matrix_btqb.set(0,1, matrix_btqb_1.get(1,0));
        matrix_btqb.set(0,2, matrix_btqb_1.get(2,0));

// System.out.println("matrix_btqb " + matrix_btqb);

        matrix_btqb = matrix_btqb.mult(matrix_b);

// System.out.println("matrix_btqb " + matrix_btqb);

        double btqb = matrix_btqb.get(0,0);
        double j = v.get(9);
        double hmb = Math.sqrt(btqb-j);

// System.out.println("double btqb " + btqb);
// System.out.println("double j " + j);
// System.out.println("double hmb " + hmb);

        eigen = matrix_q.eig();
        wr = eigen.getEigenvalues();

        SimpleMatrix vr_matrix = new SimpleMatrix(3,3);
        SimpleMatrix vr1 = eigen.getEigenVector(0);
        SimpleMatrix vr0 = eigen.getEigenVector(1);
        SimpleMatrix vr2 = eigen.getEigenVector(2);
// System.out.println("vr 0 " + vr0);
// System.out.println("vr 1 " + vr1);
// System.out.println("vr 2 " + vr2);

        double norm1 = Math.sqrt(vr0.get(0,0) * vr0.get(0,0) + vr0.get(1,0)*vr0.get(1,0) + vr0.get(2,0)*vr0.get(2,0));
        vr_matrix.set(0,0, vr0.get(0,0) / norm1);
        vr_matrix.set(1,0, vr0.get(1,0) / norm1);
        vr_matrix.set(2,0, vr0.get(2,0) / norm1);

        double norm2 = Math.sqrt(vr1.get(0,0) * vr1.get(0,0) + vr1.get(1,0)*vr1.get(1,0) + vr1.get(2,0)*vr1.get(2,0));
        vr_matrix.set(0,1, vr1.get(0,0) / norm2);
        vr_matrix.set(1,1, vr1.get(1,0) / norm2);
        vr_matrix.set(2,1, vr1.get(2,0) / norm2);

        double norm3 = Math.sqrt(vr2.get(0,0) * vr2.get(0,0) + vr2.get(1,0)*vr2.get(1,0) + vr2.get(2,0)*vr2.get(2,0));
        vr_matrix.set(0,2, vr2.get(0,0) / norm3);
        vr_matrix.set(1,2, vr2.get(1,0) / norm3);
        vr_matrix.set(2,2, vr2.get(2,0) / norm3);


// System.out.println("Matrix vr_matrix " + vr_matrix);

// System.out.println("Matrix wr " + wr);

        SimpleMatrix d_z = new SimpleMatrix(3, 3);
        d_z.set(0,0, Math.sqrt(wr.get(1).real));
        d_z.set(1,1, Math.sqrt(wr.get(0).real));
        d_z.set(2,2, Math.sqrt(wr.get(2).real));

// System.out.println("Matrix d_z " + d_z);

        SimpleMatrix matrix_sq = vr_matrix.mult(d_z).mult(vr_matrix.transpose());

// System.out.println("Matrix matrix_sq 1 " + matrix_sq);

        double hm = 0.569 / hmb;

        SimpleMatrix matrix_a_1 = new SimpleMatrix(matrix_sq);

        for(int x=0;x<matrix_a_1.getMatrix().getNumCols();x++) {
            for(int y=0;y<matrix_a_1.getMatrix().getNumRows();y++) {
                matrix_a_1.set(y, x, matrix_a_1.get(y, x) * hm);
            }
        }

// System.out.println("Matrix matrix_a_1 " + matrix_a_1);

        SimpleMatrix matrix_a = matrix_a_1.invert();

// System.out.println("Matrix a " + matrix_a);

        compassCalibration.compensation_x_x = matrix_a.get(0,0);
        compassCalibration.compensation_x_y = matrix_a.get(0,1);
        compassCalibration.compensation_x_z = matrix_a.get(0,2);

        compassCalibration.compensation_y_x = matrix_a.get(1,0);
        compassCalibration.compensation_y_y = matrix_a.get(1,1);
        compassCalibration.compensation_y_z = matrix_a.get(1,2);

        compassCalibration.compensation_z_x = matrix_a.get(2,0);
        compassCalibration.compensation_z_y = matrix_a.get(2,1);
        compassCalibration.compensation_z_z = matrix_a.get(2,2);

        return compassCalibration;
    }


    private double[] compensation(double[] axes) {
        axes[0] -= bias_x;
        axes[1] -= bias_y;
        axes[2] -= bias_z;
        axes[0] -= axes[0]*compensation_x_x + axes[1] * compensation_x_y + axes[2] * compensation_x_z;
        axes[1] -= axes[0]*compensation_y_x + axes[1] * compensation_y_y + axes[2] * compensation_y_z;
        axes[2] -= axes[0]*compensation_z_x + axes[1] * compensation_z_y + axes[2] * compensation_z_z;
        return axes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("bias_x " + this.bias_x + "\r\n");
        sb.append("bias_y " + this.bias_y + "\r\n");
        sb.append("bias_z " + this.bias_z + "\r\n");

        sb.append("compensation_x_x " + this.compensation_x_x + "\r\n");
        sb.append("compensation_x_y " + this.compensation_x_y + "\r\n");
        sb.append("compensation_x_z " + this.compensation_x_z + "\r\n");

        sb.append("compensation_y_x " + this.compensation_y_x + "\r\n");
        sb.append("compensation_y_y " + this.compensation_y_y + "\r\n");
        sb.append("compensation_y_z " + this.compensation_y_z + "\r\n");

        sb.append("compensation_z_x " + this.compensation_z_x + "\r\n");
        sb.append("compensation_z_y " + this.compensation_z_y + "\r\n");
        sb.append("compensation_z_z " + this.compensation_z_z + "\r\n");

        return sb.toString();
    }


}

