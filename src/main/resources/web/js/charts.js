/**
 * Canvas and SVG Data Visualizer for AI ResearchHub
 */
const ChartUI = {
  // Render Feature Importance Horizontal Bar Chart
  renderBarChart(canvasId, dataMap, primaryColor = '#6366f1') {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const width = canvas.width = canvas.parentElement.clientWidth || 400;
    const height = canvas.height = Math.max(220, Object.keys(dataMap).length * 40);

    ctx.clearRect(0, 0, width, height);

    const labels = Object.keys(dataMap);
    const values = Object.values(dataMap);
    const maxVal = Math.max(...values, 0.01);

    const paddingLeft = 140;
    const paddingRight = 40;
    const paddingTop = 20;
    const barHeight = 20;
    const gap = 16;

    labels.forEach((label, i) => {
      const y = paddingTop + i * (barHeight + gap);
      const val = values[i];
      const barWidth = ((width - paddingLeft - paddingRight) * (val / maxVal));

      // Label
      ctx.fillStyle = '#94a3b8';
      ctx.font = '12px Inter, sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(label.length > 16 ? label.substring(0, 14) + '...' : label, paddingLeft - 12, y + 14);

      // Bar Background
      ctx.fillStyle = 'rgba(255, 255, 255, 0.05)';
      ctx.beginPath();
      ctx.roundRect(paddingLeft, y, width - paddingLeft - paddingRight, barHeight, 4);
      ctx.fill();

      // Bar Fill
      const grad = ctx.createLinearGradient(paddingLeft, y, paddingLeft + barWidth, y);
      grad.addColorStop(0, primaryColor);
      grad.addColorStop(1, '#3b82f6');
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.roundRect(paddingLeft, y, Math.max(barWidth, 4), barHeight, 4);
      ctx.fill();

      // Value text
      ctx.fillStyle = '#f8fafc';
      ctx.font = '11px JetBrains Mono, monospace';
      ctx.textAlign = 'left';
      ctx.fillText((val * 100).toFixed(1) + '%', paddingLeft + barWidth + 8, y + 14);
    });
  },

  // Render Correlation Matrix Heatmap
  renderCorrelationMatrix(containerId, matrix) {
    const container = document.getElementById(containerId);
    if (!container || !matrix) return;

    const cols = Object.keys(matrix);
    if (cols.length === 0) return;

    let html = `<div style="overflow-x: auto;"><table class="data-table" style="text-align: center;">`;
    html += `<thead><tr><th style="text-align: left;">Feature</th>`;
    cols.forEach(c => html += `<th>${c}</th>`);
    html += `</tr></thead><tbody>`;

    cols.forEach(r => {
      html += `<tr><td style="text-align: left; font-weight: 600;">${r}</td>`;
      cols.forEach(c => {
        const val = matrix[r][c];
        let bgColor = 'rgba(255, 255, 255, 0.05)';
        let textColor = '#94a3b8';

        if (val > 0.7) {
          bgColor = `rgba(99, 102, 241, ${Math.min(0.8, val)})`;
          textColor = '#ffffff';
        } else if (val > 0.3) {
          bgColor = `rgba(59, 130, 246, ${Math.min(0.5, val)})`;
          textColor = '#ffffff';
        } else if (val < -0.3) {
          bgColor = `rgba(244, 63, 94, ${Math.min(0.6, Math.abs(val))})`;
          textColor = '#ffffff';
        }

        html += `<td style="background-color: ${bgColor}; color: ${textColor}; font-family: 'JetBrains Mono', monospace; font-size: 11.5px;">${val.toFixed(2)}</td>`;
      });
      html += `</tr>`;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
  },

  // Render Confusion Matrix Visualizer
  renderConfusionMatrix(containerId, cm) {
    const container = document.getElementById(containerId);
    if (!container || !cm) return;

    const tn = cm[0] ? cm[0][0] : 0;
    const fp = cm[0] ? cm[0][1] : 0;
    const fn = cm[1] ? cm[1][0] : 0;
    const tp = cm[1] ? cm[1][1] : 0;

    let html = `
      <div style="display: flex; flex-direction: column; align-items: center; margin: 12px 0;">
        <table class="cm-table">
          <thead>
            <tr>
              <th rowspan="2" style="background: transparent; border: none; font-size: 11px; text-transform: uppercase;">Actual Class</th>
              <th colspan="2" style="font-size: 11px; text-transform: uppercase;">Predicted Class</th>
            </tr>
            <tr>
              <th>Negative (0)</th>
              <th>Positive (1)</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td style="font-weight: 600;">Negative (0)</td>
              <td class="cm-cell-hit">TN: ${tn}</td>
              <td class="cm-cell-miss">FP: ${fp}</td>
            </tr>
            <tr>
              <td style="font-weight: 600;">Positive (1)</td>
              <td class="cm-cell-miss">FN: ${fn}</td>
              <td class="cm-cell-hit">TP: ${tp}</td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
    container.innerHTML = html;
  },

  // Render Multi-Experiment Metrics Comparison Bars
  renderComparisonChart(canvasId, experiments) {
    const canvas = document.getElementById(canvasId);
    if (!canvas || !experiments || experiments.length === 0) return;
    const ctx = canvas.getContext('2d');
    const width = canvas.width = canvas.parentElement.clientWidth || 600;
    const height = canvas.height = 260;

    ctx.clearRect(0, 0, width, height);

    const colors = ['#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6'];
    const paddingLeft = 60;
    const paddingBottom = 40;
    const paddingTop = 30;
    const chartHeight = height - paddingBottom - paddingTop;
    const chartWidth = width - paddingLeft - 40;

    const barWidth = Math.min(50, (chartWidth / experiments.length) - 20);

    // Baseline axis
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(paddingLeft, height - paddingBottom);
    ctx.lineTo(width - 20, height - paddingBottom);
    ctx.stroke();

    experiments.forEach((exp, i) => {
      const val = exp.primaryMetricValue || 0;
      const x = paddingLeft + (i * (chartWidth / experiments.length)) + 20;
      const bHeight = Math.max(8, chartHeight * Math.min(1.0, Math.max(0.0, val)));
      const y = height - paddingBottom - bHeight;

      // Bar
      ctx.fillStyle = colors[i % colors.length];
      ctx.beginPath();
      ctx.roundRect(x, y, barWidth, bHeight, [4, 4, 0, 0]);
      ctx.fill();

      // Value
      ctx.fillStyle = '#fff';
      ctx.font = '11.5px JetBrains Mono, monospace';
      ctx.textAlign = 'center';
      ctx.fillText(val.toFixed(2), x + (barWidth / 2), y - 8);

      // Label
      ctx.fillStyle = '#94a3b8';
      ctx.font = '11px Inter, sans-serif';
      const label = exp.algorithm ? exp.algorithm.substring(0, 10) : `Exp ${i+1}`;
      ctx.fillText(label, x + (barWidth / 2), height - paddingBottom + 18);
    });
  }
};
