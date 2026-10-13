package com.ucsal.avaliacaon1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WaterProgressView waterProgressView;
    private EditText editConsumo;
    private EditText editMeta;
    private TextView textAcumulado;
    private TextView textHistorico;

    private String dataCarregada;
    private WaterDatabaseHelper databaseHelper;

    private int metaDiaria;
    private int consumoAcumulado;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable verificarDataRunnable = new Runnable() {
        @Override
        public void run() {
            verificarMudancaDeDia();
            handler.postDelayed(this, 60_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        waterProgressView = findViewById(R.id.waterProgressView);
        editConsumo = findViewById(R.id.editConsumo);
        editMeta = findViewById(R.id.editMeta);
        textAcumulado = findViewById(R.id.textAcumulado);
        textHistorico = findViewById(R.id.textHistorico);

        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        Button btnSalvarMeta = findViewById(R.id.btnSalvarMeta);

        databaseHelper = new WaterDatabaseHelper(this);
        databaseHelper.limparRegistrosAntigos();
        dataCarregada = obterDataAtual();

        metaDiaria = databaseHelper.buscarMeta();
        consumoAcumulado = databaseHelper.buscarConsumoDoDia();

        editMeta.setText(String.valueOf(metaDiaria));

        atualizarInterface();

        btnRegistrar.setOnClickListener(v -> registrarConsumo());
        btnSalvarMeta.setOnClickListener(v -> salvarMeta());
    }

    private void registrarConsumo() {
        databaseHelper.limparRegistrosAntigos();

        String texto = editConsumo.getText().toString().trim();

        if (texto.isEmpty()) {
            editConsumo.setError(getString(R.string.volume_obrigatorio));
            return;
        }

        int volume;

        try {
            volume = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            editConsumo.setError(getString(R.string.volume_invalido));
            return;
        }

        if (volume <= 0) {
            editConsumo.setError(getString(R.string.volume_maior_zero));
            return;
        }

        boolean sucesso = databaseHelper.registrarConsumo(volume);

        if (!sucesso) {
            Toast.makeText(
                    this,
                    getString(R.string.registro_falhou),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        editConsumo.setText("");
        atualizarInterface();

        Toast.makeText(
                this,
                getString(R.string.consumo_registrado),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void salvarMeta() {
        String texto = editMeta.getText().toString().trim();

        if (texto.isEmpty()) {
            editMeta.setError(getString(R.string.meta_obrigatoria));
            return;
        }

        int novaMeta;

        try {
            novaMeta = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            editMeta.setError(getString(R.string.meta_invalida));
            return;
        }

        if (novaMeta <= 0) {
            editMeta.setError(getString(R.string.meta_maior_zero));
            return;
        }

        boolean sucesso = databaseHelper.salvarMeta(novaMeta);

        if (!sucesso) {
            Toast.makeText(
                    this,
                    getString(R.string.meta_falha),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        metaDiaria = novaMeta;
        atualizarInterface();

        Toast.makeText(
                this,
                getString(R.string.meta_atualizada),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void atualizarInterface() {
        // Busca os dados persistidos no banco.
        metaDiaria = databaseHelper.buscarMeta();
        consumoAcumulado = databaseHelper.buscarConsumoDoDia();

        // Atualiza o indicador visual.
        waterProgressView.setConsumption(
                consumoAcumulado,
                metaDiaria
        );

        // Atualiza o total e a meta.
        textAcumulado.setText(
                getString(
                        R.string.consumo_acumulado,
                        consumoAcumulado,
                        metaDiaria
                )
        );

        if (consumoAcumulado > metaDiaria) {
            textAcumulado.setTextColor(getColor(R.color.alert_color));
            textAcumulado.append(
                    "\n" + getString(R.string.meta_ultrapassada)
            );
        } else {
            textAcumulado.setTextColor(getColor(R.color.text_primary));
        }

        // Busca os registros de hoje como objetos WaterRecord.
        List<WaterRecord> registros =
                databaseHelper.buscarRegistrosDoDia();

        // Monta o texto que será exibido no histórico.
        StringBuilder historico = new StringBuilder();

        for (WaterRecord registro : registros) {
            String dataHora = registro.getDataHora();
            String horario = dataHora.substring(11, 16);

            historico.append(
                    getString(
                            R.string.registro_historico,
                            horario,
                            registro.getVolumeMl()
                    )
            ).append("\n\n");
        }

        if (historico.length() == 0) {
            textHistorico.setText(R.string.nenhum_consumo);
        } else {
            textHistorico.setText(historico.toString().trim());
        }
    }

    private String obterDataAtual() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());
    }

    private void verificarMudancaDeDia() {
        String hoje = obterDataAtual();

        if (!hoje.equals(dataCarregada)) {
            databaseHelper.limparRegistrosAntigos();
            dataCarregada = hoje;
            atualizarInterface();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            verificarMudancaDeDia();
            handler.removeCallbacks(verificarDataRunnable);
            handler.postDelayed(verificarDataRunnable, 60_000);
        }
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(verificarDataRunnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
