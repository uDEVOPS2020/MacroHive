import pandas as pd

dict = {"ts-auth-service":"1","ts-notification-service":"2","ts-route-service":"3","ts-security-service":"4","ts-order-service":"5","ts-order-other-service":"6","ts-station-service":"7","ts-train-service":"8","ts-travel-service":"9","ts-basic-service":"10","ts-ticketinfo-service":"11","ts-contacts-service":"12","ts-execute-service":"13","ts-route-plan-service":"14","ts-travel-plan-service":"15","ts-preserve-service":"16","ts-preserve-other-service":"17","ts-config-service":"18","ts-price-service":"19","ts-consign-price-service":"20","ts-consign-service":"21","ts-admin-order-service":"22","ts-admin-route-service":"23","ts-admin-travel-service":"24","ts-travel2-service":"25","ts-user-service":"26","ts-admin-user-service":"27","ts-inside-payment-service":"28","ts-admin-basic-info-service":"29","ts-food-map-service":"30","ts-food-service":"31","ts-cancel-service":"32","ts-rebook-service":"33","ts-assurance-service":"34","ts-seat-service":"35","ts-payment-service":"36"}


def parse(info_file, out_file):

    final_df = pd.DataFrame()
    for i in range(1,37):
        f_str = str(i) + "_f"
        final_df[f_str] = ""
        final_df[f_str] = final_df[f_str].astype(int)
    
    df = pd.read_csv(info_file)

    grouped = df.groupby("sessionKey")
    
    count_propagated_f = 0
    #test
    for name, group in grouped:
        #name = numero sessionkey
        #group = dataframes
        fail_count = 0

        if len(group) > 1:

            df_it = pd.DataFrame()

            subgrouped = group.groupby('serviceName')
            for name, subgroup in subgrouped:
                if len(subgroup) > 1:
                    subgroup = subgroup.reset_index(drop = True)
                    row = subgroup.iloc[subgroup['packetResponseCode'].idxmax()]
                    serv_index = dict[row['serviceName']]

                    if(row['packetResponseCode']>=500):
                        fail_count +=1
                        df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['2']})
                    else:
                        df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['1']})

                    df_it = pd.concat([df_it, df_temp], sort=False,axis=1).reset_index(drop = True)
                else:
                    for index, row in subgroup.iterrows():
                        serv_index = dict[row['serviceName']]

                        if(row['packetResponseCode']>=500):
                            fail_count +=1
                            df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['2']})
                        else:
                            df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['1']})

                        df_it = pd.concat([df_it, df_temp], sort=False,axis=1).reset_index(drop = True)
                        # print(df_it)

            final_df = final_df.append(df_it).reset_index(drop = True).fillna(0)
        
        if fail_count >=2:
            count_propagated_f += 1

    for i in range(1,37):
        f_str = str(i) + "_f"
        final_df[f_str] = final_df[f_str].astype(int) 

    final_df = final_df.loc[:, (final_df != final_df.iloc[0]).any()] 

    final_df.to_csv(out_file,index=False)


def parse_classes(info_file, out_file):

    final_df = pd.DataFrame()
    for i in range(1,37):
        f_str = str(i) + "_f"
        final_df[f_str] = ""
        final_df[f_str] = final_df[f_str].astype(int)

    df = pd.read_csv(info_file)

    grouped = df.groupby("sessionKey")

    count_propagated_f = 0
    #test
    for name, group in grouped:
        #name = numero sessionkey
        #group = dataframes
        fail_count = 0

        if len(group) > 1:

            df_it = pd.DataFrame()

            subgrouped = group.groupby('serviceName')
            for name, subgroup in subgrouped:
                if len(subgroup) > 1:
                    subgroup = subgroup.reset_index(drop = True)
                    row = subgroup.iloc[subgroup['packetResponseCode'].idxmax()]
                    serv_index = dict[row['serviceName']]

                    if(row['packetResponseCode']>=400):
                        fail_count +=1
                        df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['2']})
                    else:
                        df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['1']})

                    df_it = pd.concat([df_it, df_temp], sort=False,axis=1).reset_index(drop = True)
                else:
                    for index, row in subgroup.iterrows():
                        serv_index = dict[row['serviceName']]

                        if(row['packetResponseCode']>=400):
                            fail_count +=1
                            df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['2']})
                        else:
                            df_temp = pd.DataFrame({row['serviceName'] + '_f' : ['1']})

                        df_it = pd.concat([df_it, df_temp], sort=False,axis=1).reset_index(drop = True)

            final_df = final_df.append(df_it).reset_index(drop = True).fillna(0)
        
        if fail_count >=2:
            count_propagated_f += 1

    for i in range(1,37):
        f_str = str(i) + "_f"
        final_df[f_str] = final_df[f_str].astype(int) 

    final_df = final_df.loc[:, (final_df != final_df.iloc[0]).any()] 

    final_df.to_csv(out_file,index=False)