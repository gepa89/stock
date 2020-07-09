package py.com.chacomer.stock.Data;

public class AppConfig {
    // Server user login url
    //Verficacion de pedido

    public static String URL_LOGIN = "http://192.168.12.50/stock/v_pedido/get_data.php";
    public static String URL_GET_PEDIDO = "http://192.168.12.50/stock/v_pedido/get_pedido.php";
    public static String URL_SEND_PEDIDO = "http://192.168.12.50/stock/v_pedido/send_pedido.php";
    public static String URL_GET_MATERIAL = "http://192.168.12.50/stock/v_pedido/get_material.php";
    public static String URL_SEND_MATERIAL = "http://192.168.12.50/stock/v_pedido/send_material.php";

    //Control de Stock
    public static String URL_CLOSE_DOCUMENT = "http://192.168.12.50/stock/c_stock/close_document.php";
    public static String URL_CLOSE_DOC_REC = "http://192.168.12.50/stock/c_stock/close_doc_rec.php";
    public static String URL_CLOSE_PALLET = "http://192.168.12.50/stock/c_stock/close_pallet.php";
    public static String URL_CKECK_MATERIAL = "http://192.168.12.50/stock/c_stock/get_material.php";
    public static String URL_CKECK_MATERIAL_REC = "http://192.168.12.50/stock/c_stock/get_material_rec.php";
    public static String URL_GET_OC = "http://192.168.12.50/stock/c_stock/get_oc.php";
    public static String URL_SAVE_MATERIAL = "http://192.168.12.50/stock/c_stock/save_material.php";
    public static String URL_SAVE_MATERIAL_REC = "http://192.168.12.50/stock/c_stock/save_material_rec.php";
    public static String URL_CREATE_LINEAL = "http://192.168.12.50/stock/c_stock/create_lineal.php";
    public static String URL_GET_CENTROS = "http://192.168.12.50/stock/c_stock/get_centros.php";
    public static String URL_GET_LAST_DOC = "http://192.168.12.50/stock/c_stock/get_latets.php";
    public static String URL_GET_ALMACENES = "http://192.168.12.50/stock/c_stock/get_almacenes.php";
    public static String URL_LOGIN_CSTOCK = "http://192.168.12.50/stock/c_stock/get_data.php";
    public static String URL_CKECK_PEDIDO = "http://192.168.12.50/stock/c_stock/get_orden.php";
    public static String URL_POSICION = "http://192.168.12.50/stock/c_stock/get_material_list.php";
    public static String URL_POSICION_RESTART = "http://192.168.12.50/stock/c_stock/get_material_list_restart.php";
    public static String URL_CHECK_POSICION = "http://192.168.12.50/stock/c_stock/check_material.php";
    public static String URL_SEND_POSICION = "http://192.168.12.50/stock/c_stock/send_material.php";
    public static String URL_SEND_EAN = "http://192.168.12.50/stock/c_stock/send_ean.php";
    public static String URL_GET_LIST_MATERIAL = "http://192.168.12.50/stock/c_stock/check_material.php";


    // Server user register url
    public static String URL_REGISTER = "http://192.168.0.102/android_login_api/register.php";

}