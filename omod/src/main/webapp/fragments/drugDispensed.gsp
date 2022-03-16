
<style type="text/css">
    .info-body.active-drug-orders h4 {
    font-size: 1em;
    }
    .info-body.active-drug-orders h4:first-child {
    margin-top: 0px;
    }
</style>

<div class="info-section">

    <div class="info-header">
        <i class="icon-medicine"></i>
        <h3>MEDICAMENTS DISPENSES</h3>
    </div>

    <div class="info-body active-drug-orders">
        <% if (!dispensedDrugs) { %>
        ${ ui.message("emr.none") }
        <% } else { %>

        <% if (dispensedDrugs) { %>
        <% dispensedDrugs.each { %>
        
            <span class="title">Visite du ${ ui.format(it.key) }</span>
            <table>
                <tr>
                    <th>Date</th>
                    <th>Médicament</th>
                </tr>

                <tr>
                    <% def text = "- "; %>
                    <% def drug = ""; %>
                    <% it.value.each { entry -> %>
                        
                        <td>${ ui.format(entry.date) }</td>
                        <% entry.sendingDetails.each { sendingDetail -> %>
                            <% text = text + sendingDetail.item.name + '(Qte: '+ sendingDetail.sendingDetailsQuantity + '), \n - '%>
                        <% } %>
                        <td>${ ui.format(text) }</td>
                    <% } %>
                </tr>

            </table>

        <% } %>
        <% } else { %>
            <p colspan="4">${ ui.message("general.none") }</p>
        <% } %>


        <% } %>


    </div>







</div>
