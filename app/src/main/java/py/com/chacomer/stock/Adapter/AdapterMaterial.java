package py.com.chacomer.stock.Adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import py.com.chacomer.stock.Activities.CheckLocation;
import py.com.chacomer.stock.Entities.Materials;
import py.com.chacomer.stock.R;

import java.util.List;

/**
 * Created by kike on 20/01/17.
 */
public class AdapterMaterial extends BaseAdapter{
    private Context _context;
    private List<Materials> _listData; // header titles
    // child data in format of header title, child title
    Materials data = new Materials();
    ViewHolder holder = null;

    public AdapterMaterial(Context context, List<Materials> listChildData) {
        this._context = context;

        this._listData = listChildData;
    }
    @Override
    public int getCount() { return _listData.size(); }

    @Override
    public Object getItem(int position) { return null; }

    @Override
    public long getItemId(int position) { return 0; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {

            data = new Materials();
            data = _listData.get(position);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.activity_item, null);
                holder = new AdapterMaterial.ViewHolder();
                holder.txt_Name = convertView.findViewById(R.id.tvDesc);
                holder.txt_Ubicacion = convertView.findViewById(R.id.tvUbi);
                holder.txt_Centro = convertView.findViewById(R.id.tvCentro);
                holder.txt_Almacen = convertView.findViewById(R.id.tvAlm);
                holder.txt_Codmat = convertView.findViewById(R.id.tvCodMat);
                holder.txt_Stock = convertView.findViewById(R.id.tvStock);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name =  data.toString();
            Log.w("txt", ""+data.getUbicacion());
            holder.txt_Name.setText(data.getName());
            holder.txt_Centro.setText("Centro: "+data.getCentro());
            holder.txt_Almacen.setText("Almacen: "+data.getAlmacen());
            holder.txt_Ubicacion.setText("Ub.: "+data.getUbicacion());
            holder.txt_Codmat.setText("--> "+data.getCodigo());
            holder.txt_Stock.setText("Stock: "+data.getStock());

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public class ViewHolder {
        public TextView txt_Name;
        public TextView txt_Ubicacion;
        public TextView txt_Centro;
        public TextView txt_Almacen;
        public TextView txt_Codmat;
        public TextView txt_Stock;
    }
}
